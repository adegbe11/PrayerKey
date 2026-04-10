/**
 * PrayerKey Socket.io Server
 * Deploy on Railway as a standalone Node.js service.
 * Start: ts-node server/socket.ts
 */

import { createServer } from "http";
import { Server, type Socket } from "socket.io";
import { DeepgramStreamer } from "../lib/stt/deepgram-stream";
import { prisma } from "../lib/prisma";
import type {
  ServerToClientEvents,
  ClientToServerEvents,
  VerseDetectedPayload,
  ProjectorTheme,
} from "../types/sermon";

const PORT = Number(process.env.PORT ?? 3001);
const ALLOWED_ORIGINS = [
  "https://prayerkey.com",
  "http://localhost:3000",
  "http://localhost:3002",
];

// ── HTTP server + Socket.io ──────────────────────────────────────────
const httpServer = createServer((req, res) => {
  if (req.url === "/health") {
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify({ status: "ok", connections: io.engine.clientsCount }));
  } else {
    res.writeHead(404);
    res.end();
  }
});

const io = new Server<ClientToServerEvents, ServerToClientEvents>(httpServer, {
  cors: {
    origin:      ALLOWED_ORIGINS,
    methods:     ["GET", "POST"],
    credentials: true,
  },
  transports: ["websocket", "polling"],
});

// ── Per-service state ────────────────────────────────────────────────
interface ServiceState {
  versesDetected:      number;
  powerMoments:        number;
  startedAt:           number;
  attendees:           Set<string>;
  streamers:           Map<string, DeepgramStreamer>;
  translationBySocket: Map<string, string>;   // per-socket translation preference
}

const serviceState = new Map<string, ServiceState>();

function getOrCreateState(serviceId: string): ServiceState {
  if (!serviceState.has(serviceId)) {
    serviceState.set(serviceId, {
      versesDetected:      0,
      powerMoments:        0,
      startedAt:           Date.now(),
      attendees:           new Set(),
      streamers:           new Map(),
      translationBySocket: new Map(),
    });
  }
  return serviceState.get(serviceId)!;
}

// ── Connection handler ────────────────────────────────────────────────
io.on("connection", (socket: Socket<ClientToServerEvents, ServerToClientEvents>) => {
  console.log(`[socket] Client connected: ${socket.id}`);

  // ── Join a service room ──────────────────────────────────────────
  socket.on("service:join", (serviceId) => {
    socket.join(`service:${serviceId}`);
    const state = getOrCreateState(serviceId);
    state.attendees.add(socket.id);
    console.log(`[socket] ${socket.id} joined service:${serviceId}`);
    broadcastStats(serviceId, state);
  });

  // ── Leave a service room ─────────────────────────────────────────
  socket.on("service:leave", (serviceId) => {
    socket.leave(`service:${serviceId}`);
    const state = serviceState.get(serviceId);
    if (state) {
      state.attendees.delete(socket.id);
      const streamer = state.streamers.get(socket.id);
      if (streamer) {
        streamer.disconnect();
        state.streamers.delete(socket.id);
      }
    }
  });

  // ── Update translation preference for this socket ────────────────
  socket.on("service:translation", (translation, serviceId) => {
    const state = getOrCreateState(serviceId);
    state.translationBySocket.set(socket.id, translation);

    // Restart streamer so new translation takes effect immediately
    const existing = state.streamers.get(socket.id);
    if (existing) {
      existing.disconnect();
      state.streamers.delete(socket.id);
      console.log(`[socket] ${socket.id} restarted streamer with translation: ${translation}`);
    }
  });

  // ── Receive raw audio chunk from microphone ──────────────────────
  socket.on("audio:chunk", async (chunk) => {
    const rooms = [...socket.rooms].filter((r) => r.startsWith("service:"));
    if (!rooms.length) return;

    const serviceId = rooms[0].replace("service:", "");
    const state     = getOrCreateState(serviceId);

    // Lazy-init Deepgram streamer using this socket's translation
    if (!state.streamers.has(socket.id)) {
      const translation = state.translationBySocket.get(socket.id) ?? "NIV";

      const streamer = new DeepgramStreamer({
        translation,

        onVerse: async (payload: VerseDetectedPayload, isSuggestion: boolean) => {
          const state = getOrCreateState(serviceId);
          state.versesDetected++;
          if (payload.confidence >= 0.90) state.powerMoments++;

          const event = isSuggestion ? "verse:suggested" : "verse:detected";
          io.to(`service:${serviceId}`).emit(event, payload);

          prisma.verseDetection.create({
            data: {
              serviceId,
              verseRef:          payload.verseRef,
              verseText:         payload.verseText,
              translation:       payload.translation,
              confidence:        payload.confidence,
              transcriptSnippet: payload.snippetUsed,
              detectionMs:       payload.detectionMs,
            },
          }).catch((e: unknown) => console.error("[socket] DB write error:", e));

          broadcastStats(serviceId, state);
        },

        onTranscript: (text, isFinal) => {
          io.to(`service:${serviceId}`).emit("transcript:update", { text, isFinal });
        },
      });

      await streamer.connect();
      state.streamers.set(socket.id, streamer);
    }

    const streamer = state.streamers.get(socket.id);
    streamer?.sendAudio(chunk instanceof Buffer ? chunk : Buffer.from(chunk as ArrayBuffer));
  });

  // ── Direct transcript text (fallback when no mic) ────────────────
  socket.on("transcript:send", async (text, serviceId) => {
    if (!text.trim()) return;
    io.to(`service:${serviceId}`).emit("transcript:update", { text, isFinal: true });

    const { detectVerseFromText } = await import("../lib/ai/verse-detection");
    const state       = getOrCreateState(serviceId);
    const translation = state.translationBySocket.get(socket.id) ?? "NIV";
    const result      = await detectVerseFromText(text, translation);

    if (result.detected && result.confidence >= 0.75) {
      state.versesDetected++;
      const payload: VerseDetectedPayload = {
        verseRef:    result.verseRef,
        verseText:   result.verseText,
        translation: result.translation,
        confidence:  result.confidence,
        detectionMs: result.detectionMs,
        snippetUsed: result.snippetUsed,
        detectedAt:  new Date().toISOString(),
      };
      io.to(`service:${serviceId}`).emit("verse:detected", payload);
      broadcastStats(serviceId, state);
    }
  });

  // ── Manual verse push to projector ───────────────────────────────
  socket.on("verse:push", (payload, serviceId) => {
    io.to(`service:${serviceId}`).emit("verse:display", payload);
    console.log(`[socket] Manual verse push: ${payload.verseRef} to service:${serviceId}`);
  });

  // ── Projector theme update ────────────────────────────────────────
  socket.on("projector:theme", (theme: ProjectorTheme, serviceId: string) => {
    io.to(`service:${serviceId}`).emit("projector:theme", theme);
    console.log(`[socket] Projector theme updated for service:${serviceId}`);
  });

  // ── Disconnect cleanup ────────────────────────────────────────────
  socket.on("disconnect", () => {
    console.log(`[socket] Client disconnected: ${socket.id}`);
    for (const [serviceId, state] of serviceState.entries()) {
      state.attendees.delete(socket.id);
      state.translationBySocket.delete(socket.id);
      const streamer = state.streamers.get(socket.id);
      if (streamer) {
        streamer.disconnect();
        state.streamers.delete(socket.id);
      }
      if (state.attendees.size === 0) {
        serviceState.delete(serviceId);
      }
    }
  });
});

// ── Helpers ───────────────────────────────────────────────────────────
function broadcastStats(serviceId: string, state: ServiceState) {
  io.to(`service:${serviceId}`).emit("service:stats", {
    versesDetected: state.versesDetected,
    powerMoments:   state.powerMoments,
    duration:       Math.floor((Date.now() - state.startedAt) / 1000),
    attendees:      state.attendees.size,
  });
}

// ── Start ─────────────────────────────────────────────────────────────
httpServer.listen(PORT, () => {
  console.log(`\n🔴 PrayerKey Socket.io server running on port ${PORT}`);
  console.log(`   Health: http://localhost:${PORT}/health\n`);
});
