import {
  View, Text, StyleSheet, ScrollView, TouchableOpacity,
} from "react-native";
import { useAuth } from "@/hooks/useAuth";
import { router } from "expo-router";
import { Colors, Spacing, Radius, FontSize } from "@/constants/theme";
import { useSafeAreaInsets } from "react-native-safe-area-context";

function QuickCard({
  icon, title, sub, color, onPress,
}: { icon: string; title: string; sub: string; color: string; onPress: () => void }) {
  return (
    <TouchableOpacity
      style={[styles.quickCard, { borderLeftColor: color }]}
      onPress={onPress}
      activeOpacity={0.8}
    >
      <Text style={styles.quickIcon}>{icon}</Text>
      <View style={{ flex: 1 }}>
        <Text style={styles.quickTitle}>{title}</Text>
        <Text style={styles.quickSub}>{sub}</Text>
      </View>
      <Text style={[styles.quickArrow, { color }]}>›</Text>
    </TouchableOpacity>
  );
}

export default function HomeScreen() {
  const { user } = useAuth();
  const insets   = useSafeAreaInsets();

  const greeting = (() => {
    const h = new Date().getHours();
    if (h < 12) return "Good morning";
    if (h < 17) return "Good afternoon";
    return "Good evening";
  })();

  return (
    <ScrollView
      style={styles.root}
      contentContainerStyle={[styles.content, { paddingTop: insets.top + Spacing.md }]}
      showsVerticalScrollIndicator={false}
    >

      {/* Header */}
      <View style={styles.header}>
        <View>
          <Text style={styles.greeting}>{greeting},</Text>
          <Text style={styles.name}>{user?.name ?? user?.email?.split("@")[0] ?? "Friend"} 🙏</Text>
        </View>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>
            {(user?.name ?? user?.email ?? "?").slice(0, 1).toUpperCase()}
          </Text>
        </View>
      </View>

      {/* Hero banner */}
      <View style={styles.hero}>
        <View style={styles.heroCross}>
          <View style={styles.heroCrossV} />
          <View style={styles.heroCrossH} />
        </View>
        <View>
          <Text style={styles.heroLabel}>PrayerKey</Text>
          <Text style={styles.heroTitle}>Your church{"\n"}in your hands</Text>
        </View>
      </View>

      {/* Quick actions */}
      <Text style={styles.sectionTitle}>Quick Actions</Text>

      <QuickCard
        icon="✦"
        title="Generate a Prayer"
        sub="Let AI craft a personal prayer for you"
        color={Colors.gold}
        onPress={() => router.push("/(tabs)/pray")}
      />
      <QuickCard
        icon="●"
        title="Join Live Service"
        sub="Stream verses in real-time"
        color={Colors.red}
        onPress={() => router.push("/(tabs)/live")}
      />
      <QuickCard
        icon="🙌"
        title="Community"
        sub="Testimonies and prayer wall"
        color={Colors.purple}
        onPress={() => router.push("/(tabs)/community")}
      />
      <QuickCard
        icon="♥"
        title="Give"
        sub="Support your church funds"
        color={Colors.green}
        onPress={() => router.push("/(tabs)/give")}
      />

      <View style={{ height: Spacing.xl }} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  root:    { flex: 1, backgroundColor: Colors.bg },
  content: { paddingHorizontal: Spacing.lg },

  header:   { flexDirection: "row", justifyContent: "space-between", alignItems: "center", marginBottom: Spacing.xl },
  greeting: { fontSize: FontSize.base, color: Colors.t2 },
  name:     { fontSize: FontSize.xl, fontWeight: "700", color: Colors.t1 },
  avatar:   {
    width: 42, height: 42, borderRadius: 21,
    backgroundColor: Colors.gold,
    alignItems: "center", justifyContent: "center",
  },
  avatarText: { color: "#fff", fontWeight: "700", fontSize: FontSize.md },

  hero: {
    backgroundColor: Colors.t1, borderRadius: Radius.xl,
    padding: Spacing.lg, marginBottom: Spacing.xl,
    flexDirection: "row", alignItems: "center", gap: Spacing.lg,
  },
  heroCross:  { width: 40, height: 40, alignItems: "center", justifyContent: "center" },
  heroCrossV: { position: "absolute", width: 5, height: 34, borderRadius: 3, backgroundColor: Colors.gold },
  heroCrossH: { position: "absolute", top: 8, width: 28, height: 5, borderRadius: 3, backgroundColor: Colors.gold },
  heroLabel:  { fontSize: FontSize.xs, color: Colors.gold, fontWeight: "700", letterSpacing: 1.5, textTransform: "uppercase", marginBottom: 4 },
  heroTitle:  { fontSize: FontSize.xl, fontWeight: "700", color: "#FFFFFF", lineHeight: 28 },

  sectionTitle: { fontSize: FontSize.sm, fontWeight: "700", color: Colors.t3, letterSpacing: 0.8, textTransform: "uppercase", marginBottom: Spacing.sm },

  quickCard: {
    backgroundColor: Colors.card, borderRadius: Radius.lg,
    padding: Spacing.md, marginBottom: Spacing.sm,
    flexDirection: "row", alignItems: "center", gap: Spacing.md,
    borderLeftWidth: 3,
    shadowColor: Colors.shadowColor, shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.07, shadowRadius: 4, elevation: 2,
  },
  quickIcon:  { fontSize: 22 },
  quickTitle: { fontSize: FontSize.md, fontWeight: "700", color: Colors.t1, marginBottom: 2 },
  quickSub:   { fontSize: FontSize.sm, color: Colors.t3 },
  quickArrow: { fontSize: 22, fontWeight: "300" },
});
