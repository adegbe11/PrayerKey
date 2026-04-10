import {
  View, Text, StyleSheet, ScrollView, TouchableOpacity,
  TextInput, ActivityIndicator, Alert,
} from "react-native";
import { useState, useEffect, useRef } from "react";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { io, type Socket } from "socket.io-client";
import { Colors, Spacing, Radius, FontSize } from "@/constants/theme";

const SOCKET_URL = process.env.EXPO_PUBLIC_SOCKET_URL ?? process.env.EXPO_PUBLIC_API_URL ?? "http://localhost:3000";

interface VerseEvent {
  ref: string;
  text: string;
  translation: string;
  timestamp: number;
}

interface TranscriptEvent {
  text: string;
  timestamp: number;
}

export default function LiveScreen() {
  const insets = useSafeAreaInsets();
  const [serviceId,    setServiceId]    = useState("");
  const [joined,       setJoined]       = useState(false);
  const [connecting,   setConnecting]   = useState(false);
  const [verses,       setVerses]       = useState<VerseEvent[]>([]);
  const [transcripts,  setTranscripts]  = useState<TranscriptEvent[]>([]);
  const [activeVerse,  setActiveVerse]  = useState<VerseEvent | null>(null);
  const [tab,          setTab]          = useState<"verses" | "transcript">("verses");
  const socketRef   = useRef<Socket | null>(null);
  const scrollRef   = useRef<ScrollView>(null);

  function handleJoin() {
    if (!serviceId.trim()) return;
    setConnecting(true);

    const socket = io(SOCKET_URL, {
      transports: ["websocket"],
      reconnection: true,
    });

    socket.on("connect", () => {
      socket.emit("join-service", serviceId.trim());
      setJoined(true);
      setConnecting(false);
    });

    socket.on("verse-detected", (data: VerseEvent) => {
      setActiveVerse(data);
      setVerses((v) => [{ ...data, timestamp: Date.now() }, ...v].slice(0, 50));
      setTimeout(() => setActiveVerse(null), 8000);
    });

    socket.on("transcript", (data: { text: string }) => {
      setTranscripts((t) => [{ text: data.text, timestamp: Date.now() }, ...t].slice(0, 100));
    });

    socket.on("service-ended", () => {
      Alert.alert("Service Ended", "The live service has ended. God bless you!");
      handleLeave();
    });

    socket.on("connect_error", () => {
      setConnecting(false);
      Alert.alert("Connection Error", "Could not connect to the live service.");
    });

    socketRef.current = socket;
  }

  function handleLeave() {
    socketRef.current?.disconnect();
    socketRef.current = null;
    setJoined(false);
    setVerses([]);
    setTranscripts([]);
    setActiveVerse(null);
    setServiceId("");
  }

  useEffect(() => {
    return () => { socketRef.current?.disconnect(); };
  }, []);

  /* ── Join screen ── */
  if (!joined) {
    return (
      <View style={[styles.root, styles.center, { paddingTop: insets.top }]}>
        <View style={styles.liveDot} />
        <Text style={styles.joinTitle}>Join Live Service</Text>
        <Text style={styles.joinSub}>Enter the Service ID shown on screen by your pastor.</Text>

        <TextInput
          style={styles.codeInput}
          placeholder="Service ID"
          placeholderTextColor={Colors.t3}
          value={serviceId}
          onChangeText={setServiceId}
          autoCapitalize="none"
          autoCorrect={false}
        />

        <TouchableOpacity
          style={[styles.joinBtn, (!serviceId.trim() || connecting) && styles.joinBtnDisabled]}
          onPress={handleJoin}
          disabled={!serviceId.trim() || connecting}
          activeOpacity={0.8}
        >
          {connecting
            ? <ActivityIndicator color="#fff" />
            : <Text style={styles.joinBtnText}>● Join Live</Text>
          }
        </TouchableOpacity>

        <Text style={styles.hint}>Ask your church admin for the Service ID.</Text>
      </View>
    );
  }

  /* ── Live feed ── */
  return (
    <View style={[styles.root, { paddingTop: insets.top }]}>

      {/* Status bar */}
      <View style={styles.statusBar}>
        <View style={styles.liveRow}>
          <View style={styles.livePill}>
            <View style={styles.liveDotSmall} />
            <Text style={styles.liveText}>LIVE</Text>
          </View>
          <Text style={styles.serviceIdText} numberOfLines={1}>ID: {serviceId}</Text>
        </View>
        <TouchableOpacity onPress={handleLeave} style={styles.leaveBtn} activeOpacity={0.8}>
          <Text style={styles.leaveBtnText}>Leave</Text>
        </TouchableOpacity>
      </View>

      {/* Active verse spotlight */}
      {activeVerse && (
        <View style={styles.spotlight}>
          <Text style={styles.spotlightRef}>{activeVerse.ref}</Text>
          <Text style={styles.spotlightText} numberOfLines={4}>{activeVerse.text}</Text>
          <Text style={styles.spotlightTranslation}>{activeVerse.translation}</Text>
        </View>
      )}

      {/* Tab switcher */}
      <View style={styles.tabs}>
        {(["verses", "transcript"] as const).map((t) => (
          <TouchableOpacity
            key={t}
            style={[styles.tabBtn, tab === t && styles.tabBtnActive]}
            onPress={() => setTab(t)}
            activeOpacity={0.8}
          >
            <Text style={[styles.tabText, tab === t && styles.tabTextActive]}>
              {t === "verses" ? `Verses (${verses.length})` : "Transcript"}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      <ScrollView ref={scrollRef} contentContainerStyle={styles.list}>
        {tab === "verses" ? (
          verses.length > 0
            ? verses.map((v, i) => (
                <View key={i} style={styles.verseCard}>
                  <Text style={styles.verseRef}>{v.ref} · {v.translation}</Text>
                  <Text style={styles.verseText}>{v.text}</Text>
                </View>
              ))
            : <Text style={styles.empty}>Verses will appear here as the pastor preaches.</Text>
        ) : (
          transcripts.length > 0
            ? transcripts.map((t, i) => (
                <View key={i} style={styles.transcriptRow}>
                  <Text style={styles.transcriptTime}>
                    {new Date(t.timestamp).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
                  </Text>
                  <Text style={styles.transcriptText}>{t.text}</Text>
                </View>
              ))
            : <Text style={styles.empty}>Live transcript will appear here.</Text>
        )}
        <View style={{ height: Spacing.xl }} />
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  root:   { flex: 1, backgroundColor: Colors.bg },
  center: { justifyContent: "center", alignItems: "center", paddingHorizontal: Spacing.lg },

  /* Join screen */
  liveDot:      { width: 16, height: 16, borderRadius: 8, backgroundColor: Colors.red, marginBottom: Spacing.md },
  joinTitle:    { fontSize: FontSize.xxl, fontWeight: "700", color: Colors.t1, marginBottom: Spacing.xs, textAlign: "center" },
  joinSub:      { fontSize: FontSize.base, color: Colors.t2, textAlign: "center", marginBottom: Spacing.xl, lineHeight: 22 },
  codeInput:    {
    width: "100%", backgroundColor: Colors.card, borderRadius: Radius.md,
    borderWidth: 1, borderColor: Colors.border, padding: Spacing.md,
    fontSize: FontSize.lg, fontWeight: "700", color: Colors.t1, marginBottom: Spacing.md,
    textAlign: "center", letterSpacing: 4,
  },
  joinBtn:        { width: "100%", backgroundColor: Colors.red, borderRadius: Radius.pill, padding: Spacing.md, alignItems: "center" },
  joinBtnDisabled:{ opacity: 0.5 },
  joinBtnText:    { color: "#fff", fontSize: FontSize.md, fontWeight: "700" },
  hint:           { fontSize: FontSize.xs, color: Colors.t3, marginTop: Spacing.md, textAlign: "center" },

  /* Status bar */
  statusBar:    { flexDirection: "row", justifyContent: "space-between", alignItems: "center", paddingHorizontal: Spacing.lg, paddingVertical: Spacing.sm, backgroundColor: Colors.card, borderBottomWidth: 0.5, borderBottomColor: Colors.border },
  liveRow:      { flexDirection: "row", alignItems: "center", gap: Spacing.sm, flex: 1 },
  livePill:     { flexDirection: "row", alignItems: "center", gap: 5, backgroundColor: `${Colors.red}18`, borderRadius: Radius.pill, paddingHorizontal: 10, paddingVertical: 4 },
  liveDotSmall: { width: 7, height: 7, borderRadius: 4, backgroundColor: Colors.red },
  liveText:     { fontSize: FontSize.xs, fontWeight: "700", color: Colors.red, letterSpacing: 1 },
  serviceIdText:{ fontSize: FontSize.xs, color: Colors.t3, flex: 1 },
  leaveBtn:     { paddingHorizontal: 14, paddingVertical: 6, borderRadius: Radius.pill, borderWidth: 1, borderColor: Colors.border },
  leaveBtnText: { fontSize: FontSize.xs, fontWeight: "700", color: Colors.t2 },

  /* Spotlight */
  spotlight: {
    backgroundColor: Colors.t1, margin: Spacing.md, borderRadius: Radius.xl,
    padding: Spacing.lg, shadowColor: Colors.shadowColor,
    shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.15, shadowRadius: 12, elevation: 6,
  },
  spotlightRef:         { fontSize: FontSize.xs, fontWeight: "700", color: Colors.gold, letterSpacing: 1, marginBottom: 6 },
  spotlightText:        { fontSize: FontSize.md, color: "#FFFFFF", lineHeight: 26, fontStyle: "italic", marginBottom: 6 },
  spotlightTranslation: { fontSize: FontSize.xs, color: `rgba(255,255,255,0.5)` },

  /* Tabs */
  tabs:        { flexDirection: "row", paddingHorizontal: Spacing.md, gap: Spacing.xs, paddingBottom: Spacing.sm },
  tabBtn:      { flex: 1, paddingVertical: 7, borderRadius: Radius.md, alignItems: "center", backgroundColor: Colors.card, borderWidth: 1, borderColor: Colors.border },
  tabBtnActive:{ backgroundColor: Colors.t1, borderColor: Colors.t1 },
  tabText:     { fontSize: FontSize.xs, fontWeight: "600", color: Colors.t2 },
  tabTextActive:{ color: "#fff" },

  list: { paddingHorizontal: Spacing.md, paddingTop: Spacing.xs },

  /* Verse cards */
  verseCard: {
    backgroundColor: Colors.card, borderRadius: Radius.md,
    padding: Spacing.md, marginBottom: Spacing.sm,
    borderLeftWidth: 3, borderLeftColor: Colors.gold,
  },
  verseRef:  { fontSize: FontSize.xs, fontWeight: "700", color: Colors.gold, marginBottom: 4 },
  verseText: { fontSize: FontSize.sm, color: Colors.t1, lineHeight: 20, fontStyle: "italic" },

  /* Transcript */
  transcriptRow: { flexDirection: "row", gap: Spacing.sm, marginBottom: 8 },
  transcriptTime:{ fontSize: FontSize.xs, color: Colors.t3, width: 44, marginTop: 2 },
  transcriptText:{ flex: 1, fontSize: FontSize.sm, color: Colors.t2, lineHeight: 20 },

  empty: { fontSize: FontSize.base, color: Colors.t3, textAlign: "center", paddingTop: 60, lineHeight: 24 },
});
