// ── Shared types used by Socket.io, API routes, and UI ──────────────

export interface VerseDetectionResult {
  detected:       boolean;
  verseRef:       string;
  verseText:      string;
  translation:    string;
  confidence:     number;   // 0–1
  snippetUsed:    string;
  detectionMs:    number;
}

export interface SermonSession {
  serviceId:    string;
  churchId:     string;
  title:        string;
  startedAt:    string;
  isLive:       boolean;
}

// Socket.io event payloads
export interface VerseDetectedPayload {
  verseRef:      string;
  verseText:     string;
  translation:   string;
  confidence:    number;
  detectionMs:   number;
  snippetUsed:   string;
  detectedAt:    string;
}

export interface TranscriptUpdatePayload {
  text:    string;
  isFinal: boolean;
}

export interface ServiceStatsPayload {
  versesDetected: number;
  powerMoments:   number;
  duration:       number;   // seconds
  attendees:      number;
}

// Projector theme — customisable display appearance (Rhema-inspired)
export interface ProjectorTheme {
  background: string;   // CSS hex color
  textColor:  string;
  refColor:   string;
  scale:      'sm' | 'md' | 'lg' | 'xl';
  animation:  'fade' | 'slide' | 'zoom';
}

export interface VerseDetectedEvent   extends VerseDetectedPayload {}
export interface TranscriptUpdateEvent extends TranscriptUpdatePayload {}
export interface ServiceStatsEvent    extends ServiceStatsPayload {}

// Socket event map — typed for both server and client
export interface ServerToClientEvents {
  "verse:detected":   (payload: VerseDetectedPayload)   => void;
  "verse:suggested":  (payload: VerseDetectedPayload)   => void;
  "verse:display":    (payload: VerseDetectedPayload)   => void;  // manual push to projector
  "transcript:update":(payload: TranscriptUpdatePayload)=> void;
  "service:stats":    (payload: ServiceStatsPayload)    => void;
  "service:ended":    ()                                => void;
  "projector:theme":  (theme: ProjectorTheme)           => void;
}

export interface ClientToServerEvents {
  "service:join":        (serviceId: string)                               => void;
  "service:leave":       (serviceId: string)                               => void;
  "service:translation": (translation: string, serviceId: string)          => void;
  "audio:chunk":         (chunk: ArrayBuffer)                              => void;
  "transcript:send":     (text: string, serviceId: string)                 => void;
  "verse:push":          (payload: VerseDetectedPayload, serviceId: string)=> void;
  "projector:theme":     (theme: ProjectorTheme, serviceId: string)        => void;
}
