import {
  View, Text, StyleSheet, ScrollView, TextInput,
  TouchableOpacity, ActivityIndicator, Alert,
} from "react-native";
import { useState } from "react";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { generatePrayer, type GeneratedPrayer } from "@/services/api";
import { Colors, Spacing, Radius, FontSize } from "@/constants/theme";

const MOODS = [
  { label: "Grateful", color: Colors.green  },
  { label: "Anxious",  color: Colors.orange },
  { label: "Hopeful",  color: Colors.blue   },
  { label: "Grieving", color: Colors.purple },
  { label: "Seeking",  color: Colors.gold   },
  { label: "Joyful",   color: "#FF2D55"     },
  { label: "Weary",    color: Colors.t3     },
  { label: "Thankful", color: Colors.teal   },
];

export default function PrayScreen() {
  const insets = useSafeAreaInsets();
  const [selectedMoods, setSelectedMoods] = useState<string[]>([]);
  const [userInput, setUserInput]         = useState("");
  const [loading, setLoading]             = useState(false);
  const [result, setResult]               = useState<GeneratedPrayer | null>(null);

  function toggleMood(label: string) {
    setSelectedMoods((p) =>
      p.includes(label) ? p.filter((m) => m !== label) : [...p, label]
    );
  }

  async function handleGenerate() {
    if (!userInput.trim()) return;
    setLoading(true);
    try {
      const res = await generatePrayer(userInput.trim(), selectedMoods);
      setResult(res);
    } catch (err) {
      Alert.alert("Error", err instanceof Error ? err.message : "Failed to generate prayer");
    } finally {
      setLoading(false);
    }
  }

  function handleReset() {
    setResult(null);
    setUserInput("");
    setSelectedMoods([]);
  }

  return (
    <ScrollView
      style={styles.root}
      contentContainerStyle={[styles.content, { paddingTop: insets.top + Spacing.md }]}
      keyboardShouldPersistTaps="handled"
    >
      {/* Header */}
      <Text style={styles.eyebrow}>Prayer AI</Text>
      <Text style={styles.title}>Pray</Text>
      <Text style={styles.sub}>Tell God what&apos;s on your heart. AI crafts a personal prayer.</Text>

      {!result ? (
        <>
          {/* Mood selector */}
          <Text style={styles.label}>How are you feeling?</Text>
          <View style={styles.moodRow}>
            {MOODS.map((m) => {
              const active = selectedMoods.includes(m.label);
              return (
                <TouchableOpacity
                  key={m.label}
                  onPress={() => toggleMood(m.label)}
                  style={[
                    styles.moodChip,
                    active && { borderColor: m.color, backgroundColor: `${m.color}18` },
                  ]}
                  activeOpacity={0.8}
                >
                  <Text style={[styles.moodText, active && { color: m.color, fontWeight: "700" }]}>
                    {m.label}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>

          {/* Prayer input */}
          <Text style={styles.label}>What&apos;s on your heart?</Text>
          <TextInput
            style={styles.textarea}
            multiline
            numberOfLines={5}
            placeholder="Share what you'd like to bring before God…"
            placeholderTextColor={Colors.t3}
            value={userInput}
            onChangeText={setUserInput}
            textAlignVertical="top"
          />

          <TouchableOpacity
            style={[styles.btn, (!userInput.trim() || loading) && styles.btnDisabled]}
            onPress={handleGenerate}
            disabled={!userInput.trim() || loading}
            activeOpacity={0.8}
          >
            {loading
              ? <ActivityIndicator color="#fff" />
              : <Text style={styles.btnText}>✦  Generate Prayer</Text>
            }
          </TouchableOpacity>
        </>
      ) : (
        <>
          {/* Encouragement */}
          <View style={styles.encouragement}>
            <Text style={styles.encourageText}>✦  {result.encouragement}</Text>
          </View>

          {/* Prayer */}
          <View style={styles.prayerCard}>
            <Text style={styles.prayerEyebrow}>Your Prayer</Text>
            <Text style={styles.prayerTitle}>{result.title}</Text>

            {selectedMoods.length > 0 && (
              <View style={styles.moodRow}>
                {selectedMoods.map((m) => {
                  const mood = MOODS.find((x) => x.label === m);
                  return (
                    <View key={m} style={[styles.moodChip, { borderColor: mood?.color ?? Colors.gold, backgroundColor: `${mood?.color ?? Colors.gold}14` }]}>
                      <Text style={[styles.moodText, { color: mood?.color ?? Colors.gold, fontWeight: "700" }]}>{m}</Text>
                    </View>
                  );
                })}
              </View>
            )}

            <View style={styles.divider} />
            <Text style={styles.prayerText}>{result.prayer}</Text>

            {/* Verses */}
            {result.verses.map((v, i) => (
              <View key={i} style={styles.verseBlock}>
                <Text style={styles.verseText}>&ldquo;{v.text}&rdquo;</Text>
                <Text style={styles.verseRef}>{v.ref} · {v.translation}</Text>
              </View>
            ))}
          </View>

          {/* Actions */}
          <View style={styles.actionsRow}>
            <TouchableOpacity style={styles.secondaryBtn} onPress={handleReset} activeOpacity={0.8}>
              <Text style={styles.secondaryBtnText}>New Prayer</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={styles.btn}
              onPress={() => Alert.alert("Copied", "Prayer copied to clipboard")}
              activeOpacity={0.8}
            >
              <Text style={styles.btnText}>Copy Prayer</Text>
            </TouchableOpacity>
          </View>
        </>
      )}

      <View style={{ height: Spacing.xl }} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  root:    { flex: 1, backgroundColor: Colors.bg },
  content: { paddingHorizontal: Spacing.lg },

  eyebrow: { fontSize: FontSize.xs, color: Colors.gold, fontWeight: "700", letterSpacing: 1.2, textTransform: "uppercase", marginBottom: 4 },
  title:   { fontSize: FontSize.hero, fontWeight: "700", color: Colors.t1, marginBottom: 6 },
  sub:     { fontSize: FontSize.base, color: Colors.t2, marginBottom: Spacing.xl },

  label:   { fontSize: FontSize.sm, fontWeight: "700", color: Colors.t2, letterSpacing: 0.5, textTransform: "uppercase", marginBottom: Spacing.sm },

  moodRow: { flexDirection: "row", flexWrap: "wrap", gap: Spacing.xs, marginBottom: Spacing.lg },
  moodChip: {
    paddingHorizontal: 12, paddingVertical: 6, borderRadius: Radius.pill,
    borderWidth: 1, borderColor: Colors.border, backgroundColor: Colors.card,
  },
  moodText: { fontSize: FontSize.sm, color: Colors.t2 },

  textarea: {
    backgroundColor: Colors.card, borderRadius: Radius.md,
    borderWidth: 1, borderColor: Colors.border,
    padding: Spacing.md, fontSize: FontSize.md, color: Colors.t1,
    minHeight: 120, marginBottom: Spacing.md,
  },

  btn:         { backgroundColor: Colors.t1, borderRadius: Radius.pill, padding: Spacing.md, alignItems: "center" },
  btnDisabled: { backgroundColor: Colors.border, opacity: 0.6 },
  btnText:     { color: "#fff", fontSize: FontSize.md, fontWeight: "700" },

  encouragement: {
    backgroundColor: `${Colors.gold}10`, borderRadius: Radius.md,
    borderWidth: 0.5, borderColor: `${Colors.gold}40`,
    padding: Spacing.md, marginBottom: Spacing.md,
  },
  encourageText: { fontSize: FontSize.base, color: Colors.t1, lineHeight: 22 },

  prayerCard: {
    backgroundColor: Colors.card, borderRadius: Radius.xl,
    padding: Spacing.lg, marginBottom: Spacing.md,
    shadowColor: Colors.shadowColor, shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08, shadowRadius: 8, elevation: 3,
  },
  prayerEyebrow: { fontSize: FontSize.xs, color: Colors.gold, fontWeight: "700", letterSpacing: 1.2, textTransform: "uppercase", marginBottom: 4 },
  prayerTitle:   { fontSize: FontSize.xl, fontWeight: "700", color: Colors.t1, marginBottom: Spacing.sm },
  divider:       { height: 1, backgroundColor: Colors.border, marginVertical: Spacing.md },
  prayerText:    { fontSize: FontSize.md, color: Colors.t1, lineHeight: 28, fontStyle: "italic", marginBottom: Spacing.md },

  verseBlock: {
    backgroundColor: Colors.bg, borderRadius: Radius.sm,
    padding: Spacing.sm + 4, borderLeftWidth: 3, borderLeftColor: Colors.gold,
    marginBottom: Spacing.sm,
  },
  verseText: { fontSize: FontSize.sm, color: Colors.t2, fontStyle: "italic", lineHeight: 20, marginBottom: 4 },
  verseRef:  { fontSize: FontSize.xs, color: Colors.gold, fontWeight: "700" },

  actionsRow:    { flexDirection: "row", gap: Spacing.sm },
  secondaryBtn:  {
    flex: 1, borderRadius: Radius.pill, padding: Spacing.md,
    alignItems: "center", borderWidth: 1, borderColor: Colors.border, backgroundColor: Colors.card,
  },
  secondaryBtnText: { color: Colors.t1, fontSize: FontSize.md, fontWeight: "600" },
});
