import {
  View, Text, StyleSheet, ScrollView, TouchableOpacity, Alert,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useAuth } from "@/hooks/useAuth";
import { Colors, Spacing, Radius, FontSize } from "@/constants/theme";
import { useState, useEffect } from "react";
import { apiFetch } from "@/services/api";

interface ProfileStats {
  prayerCount:   number;
  prayerStreak:  number;
  amenCount:     number;
  donationTotal: number;
}

export default function ProfileScreen() {
  const insets = useSafeAreaInsets();
  const { user, signOut } = useAuth();
  const [stats, setStats] = useState<ProfileStats | null>(null);

  useEffect(() => {
    apiFetch<ProfileStats>("/api/mobile/me/stats")
      .then(setStats)
      .catch(() => {});
  }, []);

  function handleSignOut() {
    Alert.alert(
      "Sign Out",
      "Are you sure you want to sign out?",
      [
        { text: "Cancel", style: "cancel" },
        { text: "Sign Out", style: "destructive", onPress: signOut },
      ]
    );
  }

  const initials = (user?.name ?? user?.email ?? "?").slice(0, 1).toUpperCase();

  return (
    <ScrollView
      style={styles.root}
      contentContainerStyle={[styles.content, { paddingTop: insets.top + Spacing.md }]}
      showsVerticalScrollIndicator={false}
    >
      {/* Avatar + name */}
      <View style={styles.heroSection}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{initials}</Text>
        </View>
        <Text style={styles.name}>{user?.name ?? user?.email?.split("@")[0] ?? "Friend"}</Text>
        <Text style={styles.email}>{user?.email ?? ""}</Text>
        {user?.role && (
          <View style={styles.roleBadge}>
            <Text style={styles.roleText}>{user.role.replace("_", " ")}</Text>
          </View>
        )}
        {user?.churchName && (
          <Text style={styles.church}>⛪  {user.churchName}</Text>
        )}
      </View>

      {/* Stats row */}
      <View style={styles.statsRow}>
        <StatCell
          value={stats?.prayerStreak ?? 0}
          label="Day Streak"
          suffix="🔥"
          color={Colors.orange}
        />
        <View style={styles.statDivider} />
        <StatCell
          value={stats?.prayerCount ?? 0}
          label="Prayers"
          suffix=""
          color={Colors.gold}
        />
        <View style={styles.statDivider} />
        <StatCell
          value={stats?.amenCount ?? 0}
          label="Amens"
          suffix="🙌"
          color={Colors.green}
        />
      </View>

      {/* Menu items */}
      <Text style={styles.sectionTitle}>Account</Text>

      <MenuItem icon="✦" label="My Prayers"    sub="View all your generated prayers" color={Colors.gold} />
      <MenuItem icon="🙏" label="My Donations"  sub="Your giving history"             color={Colors.green} />
      <MenuItem icon="●" label="Prayer Streak" sub={`${stats?.prayerStreak ?? 0} days in a row`} color={Colors.orange} />

      <Text style={[styles.sectionTitle, { marginTop: Spacing.xl }]}>More</Text>

      <MenuItem icon="⛪" label="Church Info"   sub={user?.churchName ?? "Connected church"}  color={Colors.blue} />
      <MenuItem icon="⚙"  label="Notifications" sub="Manage alerts and reminders"             color={Colors.t2} />
      <MenuItem icon="?"  label="Help & Support" sub="Get help or send feedback"              color={Colors.t3} />

      {/* Sign out */}
      <TouchableOpacity style={styles.signOutBtn} onPress={handleSignOut} activeOpacity={0.8}>
        <Text style={styles.signOutText}>Sign Out</Text>
      </TouchableOpacity>

      <Text style={styles.version}>PrayerKey v1.0.0</Text>

      <View style={{ height: Spacing.xl }} />
    </ScrollView>
  );
}

function StatCell({ value, label, suffix, color }: { value: number; label: string; suffix: string; color: string }) {
  return (
    <View style={styles.statCell}>
      <Text style={[styles.statValue, { color }]}>
        {value.toLocaleString()}{suffix ? ` ${suffix}` : ""}
      </Text>
      <Text style={styles.statLabel}>{label}</Text>
    </View>
  );
}

function MenuItem({ icon, label, sub, color }: { icon: string; label: string; sub: string; color: string }) {
  return (
    <TouchableOpacity style={styles.menuItem} activeOpacity={0.7}>
      <View style={[styles.menuIcon, { backgroundColor: `${color}18` }]}>
        <Text style={{ fontSize: 16 }}>{icon}</Text>
      </View>
      <View style={{ flex: 1 }}>
        <Text style={styles.menuLabel}>{label}</Text>
        <Text style={styles.menuSub}>{sub}</Text>
      </View>
      <Text style={styles.menuArrow}>›</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  root:    { flex: 1, backgroundColor: Colors.bg },
  content: { paddingHorizontal: Spacing.lg },

  heroSection: { alignItems: "center", marginBottom: Spacing.xl },
  avatar:      { width: 80, height: 80, borderRadius: 40, backgroundColor: Colors.gold, alignItems: "center", justifyContent: "center", marginBottom: Spacing.md },
  avatarText:  { color: "#fff", fontWeight: "700", fontSize: 32 },
  name:        { fontSize: FontSize.xl, fontWeight: "700", color: Colors.t1, marginBottom: 4 },
  email:       { fontSize: FontSize.sm, color: Colors.t2, marginBottom: Spacing.sm },
  roleBadge:   { paddingHorizontal: 12, paddingVertical: 4, borderRadius: Radius.pill, backgroundColor: `${Colors.gold}15`, borderWidth: 1, borderColor: `${Colors.gold}30`, marginBottom: 6 },
  roleText:    { fontSize: FontSize.xs, fontWeight: "700", color: Colors.gold, letterSpacing: 0.5, textTransform: "uppercase" },
  church:      { fontSize: FontSize.sm, color: Colors.t3 },

  statsRow:    { flexDirection: "row", backgroundColor: Colors.card, borderRadius: Radius.lg, marginBottom: Spacing.xl, borderWidth: 1, borderColor: Colors.border },
  statCell:    { flex: 1, alignItems: "center", paddingVertical: Spacing.md },
  statDivider: { width: 1, backgroundColor: Colors.border, marginVertical: Spacing.sm },
  statValue:   { fontSize: FontSize.lg, fontWeight: "700", marginBottom: 2 },
  statLabel:   { fontSize: FontSize.xs, color: Colors.t3 },

  sectionTitle:{ fontSize: FontSize.xs, fontWeight: "700", color: Colors.t3, letterSpacing: 0.8, textTransform: "uppercase", marginBottom: Spacing.sm },

  menuItem:  { flexDirection: "row", alignItems: "center", gap: Spacing.md, backgroundColor: Colors.card, borderRadius: Radius.md, padding: Spacing.md, marginBottom: Spacing.xs, borderWidth: 1, borderColor: Colors.border },
  menuIcon:  { width: 36, height: 36, borderRadius: Radius.sm, alignItems: "center", justifyContent: "center" },
  menuLabel: { fontSize: FontSize.md, fontWeight: "600", color: Colors.t1, marginBottom: 2 },
  menuSub:   { fontSize: FontSize.xs, color: Colors.t3 },
  menuArrow: { fontSize: 20, color: Colors.t3 },

  signOutBtn:  { marginTop: Spacing.xl, borderRadius: Radius.pill, borderWidth: 1.5, borderColor: Colors.red, padding: Spacing.md, alignItems: "center" },
  signOutText: { fontSize: FontSize.md, fontWeight: "700", color: Colors.red },

  version:   { textAlign: "center", fontSize: FontSize.xs, color: Colors.t3, marginTop: Spacing.md },
});
