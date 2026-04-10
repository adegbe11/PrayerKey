import {
  View, Text, StyleSheet, ScrollView, TouchableOpacity,
  RefreshControl, ActivityIndicator, Alert,
} from "react-native";
import { useState, useEffect, useCallback } from "react";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import {
  getTestimonies, getPrayerRequests, amenTestimony, prayForRequest,
  type Testimony, type PrayerRequest,
} from "@/services/api";
import { Colors, Spacing, Radius, FontSize } from "@/constants/theme";

type Tab = "testimonies" | "prayer-wall";

function timeAgo(d: string) {
  const diff = Math.floor((Date.now() - new Date(d).getTime()) / 1000);
  if (diff < 3600)  return `${Math.floor(diff / 60)}m ago`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
  return new Date(d).toLocaleDateString("en-US", { month: "short", day: "numeric" });
}

export default function CommunityScreen() {
  const insets = useSafeAreaInsets();
  const [tab,           setTab]           = useState<Tab>("testimonies");
  const [testimonies,   setTestimonies]   = useState<Testimony[]>([]);
  const [requests,      setRequests]      = useState<PrayerRequest[]>([]);
  const [loading,       setLoading]       = useState(true);
  const [refreshing,    setRefreshing]    = useState(false);

  const fetchData = useCallback(async () => {
    try {
      const [t, r] = await Promise.all([getTestimonies(), getPrayerRequests()]);
      setTestimonies(t);
      setRequests(r);
    } catch {
      // silent — no data shown
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  async function handleAmen(id: string) {
    try {
      await amenTestimony(id);
      setTestimonies((p) => p.map((t) => t.id === id ? { ...t, amenCount: t.amenCount + 1 } : t));
    } catch {
      Alert.alert("Error", "Could not send Amen");
    }
  }

  async function handlePray(id: string) {
    try {
      await prayForRequest(id);
      setRequests((p) => p.map((r) => r.id === id ? { ...r, prayCount: r.prayCount + 1 } : r));
    } catch {
      Alert.alert("Error", "Could not record prayer");
    }
  }

  return (
    <View style={[styles.root, { paddingTop: insets.top }]}>

      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.title}>Community</Text>
        <View style={styles.tabs}>
          {(["testimonies", "prayer-wall"] as Tab[]).map((t) => (
            <TouchableOpacity
              key={t}
              style={[styles.tabBtn, tab === t && styles.tabBtnActive]}
              onPress={() => setTab(t)}
              activeOpacity={0.8}
            >
              <Text style={[styles.tabText, tab === t && styles.tabTextActive]}>
                {t === "testimonies" ? "Testimonies" : "Prayer Wall"}
              </Text>
            </TouchableOpacity>
          ))}
        </View>
      </View>

      {loading ? (
        <ActivityIndicator color={Colors.gold} style={{ marginTop: 40 }} />
      ) : (
        <ScrollView
          contentContainerStyle={styles.list}
          refreshControl={
            <RefreshControl
              refreshing={refreshing}
              onRefresh={() => { setRefreshing(true); fetchData(); }}
              tintColor={Colors.gold}
            />
          }
        >
          {tab === "testimonies" ? (
            testimonies.length > 0
              ? testimonies.map((t) => (
                  <View key={t.id} style={styles.card}>
                    <View style={styles.cardHeader}>
                      <View style={styles.avatar}>
                        <Text style={styles.avatarText}>
                          {t.anonymous ? "?" : (t.user.name ?? "?").slice(0, 1).toUpperCase()}
                        </Text>
                      </View>
                      <View style={{ flex: 1 }}>
                        <Text style={styles.author}>{t.anonymous ? "Anonymous" : (t.user.name ?? "Member")}</Text>
                        <Text style={styles.time}>{timeAgo(t.createdAt)}</Text>
                      </View>
                      {t.amenCount > 0 && <Text style={styles.amenCount}>🙌 {t.amenCount}</Text>}
                    </View>

                    <Text style={styles.cardTitle}>{t.title}</Text>
                    <Text style={styles.cardBody} numberOfLines={4}>{t.story}</Text>

                    {t.tags.length > 0 && (
                      <View style={styles.tagRow}>
                        {t.tags.map((tag) => (
                          <View key={tag} style={styles.tag}>
                            <Text style={styles.tagText}>{tag}</Text>
                          </View>
                        ))}
                      </View>
                    )}

                    <TouchableOpacity style={styles.actionBtn} onPress={() => handleAmen(t.id)} activeOpacity={0.8}>
                      <Text style={styles.actionBtnText}>🙌  Amen</Text>
                    </TouchableOpacity>
                  </View>
                ))
              : <Text style={styles.empty}>No testimonies yet.</Text>
          ) : (
            requests.length > 0
              ? requests.map((r) => (
                  <View key={r.id} style={styles.card}>
                    <View style={styles.cardHeader}>
                      <View style={[styles.dot, { backgroundColor: Colors.blue }]} />
                      <View style={{ flex: 1 }}>
                        <Text style={styles.author}>{r.anonymous ? "Anonymous" : (r.user.name ?? "Member")}</Text>
                        <Text style={styles.time}>{timeAgo(r.createdAt)}</Text>
                      </View>
                      {r.prayCount > 0 && (
                        <View style={styles.prayBadge}>
                          <Text style={styles.prayBadgeText}>{r.prayCount} praying</Text>
                        </View>
                      )}
                    </View>

                    <Text style={styles.cardTitle}>{r.title}</Text>
                    <Text style={styles.cardBody} numberOfLines={3}>{r.body}</Text>

                    <TouchableOpacity style={[styles.actionBtn, { borderColor: `${Colors.blue}40`, backgroundColor: `${Colors.blue}08` }]} onPress={() => handlePray(r.id)} activeOpacity={0.8}>
                      <Text style={[styles.actionBtnText, { color: Colors.blue }]}>🙏  I&apos;ll Pray</Text>
                    </TouchableOpacity>
                  </View>
                ))
              : <Text style={styles.empty}>No prayer requests.</Text>
          )}
          <View style={{ height: Spacing.xl }} />
        </ScrollView>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  root:   { flex: 1, backgroundColor: Colors.bg },
  header: { paddingHorizontal: Spacing.lg, paddingTop: Spacing.md, paddingBottom: Spacing.sm, backgroundColor: Colors.bg },
  title:  { fontSize: FontSize.hero, fontWeight: "700", color: Colors.t1, marginBottom: Spacing.md },

  tabs:       { flexDirection: "row", gap: Spacing.xs },
  tabBtn:     { flex: 1, paddingVertical: 8, borderRadius: Radius.md, alignItems: "center", backgroundColor: Colors.card, borderWidth: 1, borderColor: Colors.border },
  tabBtnActive: { backgroundColor: Colors.t1, borderColor: Colors.t1 },
  tabText:    { fontSize: FontSize.sm, fontWeight: "600", color: Colors.t2 },
  tabTextActive: { color: "#fff" },

  list: { paddingHorizontal: Spacing.lg, paddingTop: Spacing.md },

  card: {
    backgroundColor: Colors.card, borderRadius: Radius.lg,
    padding: Spacing.md, marginBottom: Spacing.md,
    shadowColor: Colors.shadowColor, shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.07, shadowRadius: 4, elevation: 2,
  },
  cardHeader: { flexDirection: "row", alignItems: "center", gap: Spacing.sm, marginBottom: Spacing.sm },
  avatar:     { width: 32, height: 32, borderRadius: 16, backgroundColor: Colors.gold, alignItems: "center", justifyContent: "center" },
  avatarText: { color: "#fff", fontWeight: "700", fontSize: FontSize.sm },
  dot:        { width: 8, height: 8, borderRadius: 4, marginRight: 4 },
  author:     { fontSize: FontSize.sm, fontWeight: "700", color: Colors.t1 },
  time:       { fontSize: FontSize.xs, color: Colors.t3 },
  amenCount:  { fontSize: FontSize.sm, fontWeight: "700", color: Colors.gold },

  cardTitle:  { fontSize: FontSize.md, fontWeight: "700", color: Colors.t1, marginBottom: 4 },
  cardBody:   { fontSize: FontSize.base, color: Colors.t2, lineHeight: 22, marginBottom: Spacing.sm },

  tagRow: { flexDirection: "row", flexWrap: "wrap", gap: 4, marginBottom: Spacing.sm },
  tag:    { paddingHorizontal: 8, paddingVertical: 2, borderRadius: Radius.pill, backgroundColor: `${Colors.gold}12` },
  tagText:{ fontSize: FontSize.xs, fontWeight: "700", color: Colors.gold },

  actionBtn:     { borderWidth: 1, borderColor: `${Colors.gold}40`, borderRadius: Radius.md, padding: Spacing.sm, alignItems: "center", backgroundColor: `${Colors.gold}08` },
  actionBtnText: { fontSize: FontSize.sm, fontWeight: "700", color: Colors.gold },

  prayBadge:     { backgroundColor: `${Colors.blue}12`, borderRadius: Radius.pill, paddingHorizontal: 8, paddingVertical: 3 },
  prayBadgeText: { fontSize: FontSize.xs, fontWeight: "700", color: Colors.blue },

  empty: { fontSize: FontSize.base, color: Colors.t3, textAlign: "center", paddingTop: 40 },
});
