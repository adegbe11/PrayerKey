import {
  View, Text, StyleSheet, ScrollView, TouchableOpacity,
  TextInput, ActivityIndicator, Alert,
} from "react-native";
import { useState, useEffect } from "react";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { getGivingFunds, submitDonation, type GivingFund } from "@/services/api";
import { Colors, Spacing, Radius, FontSize } from "@/constants/theme";

const CURRENCIES = [
  { code: "USD", symbol: "$"   },
  { code: "NGN", symbol: "₦"  },
  { code: "GBP", symbol: "£"  },
  { code: "EUR", symbol: "€"  },
  { code: "GHS", symbol: "₵"  },
  { code: "KES", symbol: "KSh"},
];

const QUICK = [10, 25, 50, 100, 250, 500];

export default function GiveScreen() {
  const insets = useSafeAreaInsets();
  const [funds,    setFunds]    = useState<GivingFund[]>([]);
  const [fundId,   setFundId]   = useState("");
  const [amount,   setAmount]   = useState("");
  const [currency, setCurrency] = useState("USD");
  const [loading,  setLoading]  = useState(false);
  const [fetching, setFetching] = useState(true);
  const [done,     setDone]     = useState(false);

  useEffect(() => {
    getGivingFunds()
      .then((data) => { setFunds(data); if (data[0]) setFundId(data[0].id); })
      .catch(() => {})
      .finally(() => setFetching(false));
  }, []);

  const sym = CURRENCIES.find((c) => c.code === currency)?.symbol ?? "$";

  const fmt = (cents: number) =>
    new Intl.NumberFormat("en-US", { style: "currency", currency, maximumFractionDigits: 0 }).format(cents / 100);

  async function handleGive() {
    const parsed = parseFloat(amount);
    if (!fundId || isNaN(parsed) || parsed <= 0) return;
    setLoading(true);
    try {
      await submitDonation(fundId, parsed, currency);
      setDone(true);
      setAmount("");
    } catch (err) {
      Alert.alert("Error", err instanceof Error ? err.message : "Failed");
    } finally {
      setLoading(false);
    }
  }

  if (done) {
    return (
      <View style={[styles.root, { justifyContent: "center", alignItems: "center", paddingHorizontal: Spacing.lg }]}>
        <Text style={{ fontSize: 56 }}>🙏</Text>
        <Text style={styles.doneTitle}>Thank you for giving!</Text>
        <Text style={styles.doneSub}>Your generosity makes a difference.</Text>
        <TouchableOpacity style={[styles.btn, { marginTop: Spacing.xl }]} onPress={() => setDone(false)} activeOpacity={0.8}>
          <Text style={styles.btnText}>Give Again</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <ScrollView
      style={styles.root}
      contentContainerStyle={[styles.content, { paddingTop: insets.top + Spacing.md }]}
      keyboardShouldPersistTaps="handled"
    >
      <Text style={styles.eyebrow}>Free Giving</Text>
      <Text style={styles.title}>Give</Text>
      <Text style={styles.sub}>100% free. No fees. Every gift goes to the fund.</Text>

      {fetching ? (
        <ActivityIndicator color={Colors.gold} style={{ marginTop: 40 }} />
      ) : (
        <>
          {/* Fund selection */}
          <Text style={styles.label}>Select Fund</Text>
          {funds.map((f) => {
            const pct = f.goal ? Math.min(100, Math.round((f.raised / f.goal) * 100)) : null;
            const sel = fundId === f.id;
            return (
              <TouchableOpacity
                key={f.id}
                style={[styles.fundCard, sel && styles.fundCardActive]}
                onPress={() => setFundId(f.id)}
                activeOpacity={0.8}
              >
                <View style={styles.fundRow}>
                  <View style={{ flex: 1 }}>
                    <Text style={styles.fundName}>{f.name}</Text>
                    {f.description && <Text style={styles.fundDesc}>{f.description}</Text>}
                  </View>
                  <Text style={styles.fundRaised}>{fmt(f.raised)}</Text>
                </View>
                {pct !== null && (
                  <View style={styles.progressWrap}>
                    <View style={[styles.progressBar, { width: `${pct}%` as `${number}%`, backgroundColor: pct >= 100 ? Colors.green : Colors.gold }]} />
                  </View>
                )}
              </TouchableOpacity>
            );
          })}

          {funds.length === 0 && (
            <Text style={styles.empty}>No giving funds set up yet.</Text>
          )}

          {/* Currency */}
          <Text style={[styles.label, { marginTop: Spacing.lg }]}>Currency</Text>
          <ScrollView horizontal showsHorizontalScrollIndicator={false} style={{ marginBottom: Spacing.md }}>
            <View style={{ flexDirection: "row", gap: Spacing.sm, paddingRight: Spacing.lg }}>
              {CURRENCIES.map((c) => (
                <TouchableOpacity
                  key={c.code}
                  style={[styles.currChip, currency === c.code && styles.currChipActive]}
                  onPress={() => setCurrency(c.code)}
                  activeOpacity={0.8}
                >
                  <Text style={[styles.currText, currency === c.code && { color: Colors.gold, fontWeight: "700" }]}>
                    {c.symbol} {c.code}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>
          </ScrollView>

          {/* Quick amounts */}
          <Text style={styles.label}>Amount ({sym})</Text>
          <View style={styles.quickRow}>
            {QUICK.map((q) => (
              <TouchableOpacity
                key={q}
                style={[styles.quickChip, amount === String(q) && styles.quickChipActive]}
                onPress={() => setAmount(String(q))}
                activeOpacity={0.8}
              >
                <Text style={[styles.quickText, amount === String(q) && { color: Colors.gold, fontWeight: "700" }]}>
                  {sym}{q}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          <TextInput
            style={styles.input}
            placeholder={`${sym} Custom amount`}
            placeholderTextColor={Colors.t3}
            keyboardType="decimal-pad"
            value={amount}
            onChangeText={setAmount}
          />

          <TouchableOpacity
            style={[styles.btn, (!fundId || !amount || parseFloat(amount) <= 0 || loading) && styles.btnDisabled]}
            onPress={handleGive}
            disabled={!fundId || !amount || parseFloat(amount) <= 0 || loading}
            activeOpacity={0.8}
          >
            {loading
              ? <ActivityIndicator color="#fff" />
              : <Text style={styles.btnText}>
                  Give {amount && parseFloat(amount) > 0 ? `${sym}${parseFloat(amount).toLocaleString()}` : ""}
                </Text>
            }
          </TouchableOpacity>

          <Text style={styles.note}>Donations are recorded and tracked by your church.</Text>
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

  fundCard: {
    backgroundColor: Colors.card, borderRadius: Radius.md,
    padding: Spacing.md, marginBottom: Spacing.sm,
    borderWidth: 1, borderColor: Colors.border,
  },
  fundCardActive: { borderColor: Colors.gold, backgroundColor: `${Colors.gold}06` },
  fundRow:   { flexDirection: "row", alignItems: "flex-start", gap: Spacing.sm, marginBottom: 8 },
  fundName:  { fontSize: FontSize.md, fontWeight: "700", color: Colors.t1, marginBottom: 2 },
  fundDesc:  { fontSize: FontSize.sm, color: Colors.t3 },
  fundRaised:{ fontSize: FontSize.lg, fontWeight: "800", color: Colors.gold },
  progressWrap: { height: 4, backgroundColor: Colors.border, borderRadius: 2, overflow: "hidden" },
  progressBar:  { height: "100%", borderRadius: 2 },

  empty: { fontSize: FontSize.base, color: Colors.t3, textAlign: "center", padding: Spacing.xl },

  currChip:      { paddingHorizontal: 14, paddingVertical: 7, borderRadius: Radius.pill, borderWidth: 1, borderColor: Colors.border, backgroundColor: Colors.card },
  currChipActive:{ borderColor: Colors.gold, backgroundColor: `${Colors.gold}10` },
  currText:      { fontSize: FontSize.sm, color: Colors.t2 },

  quickRow:      { flexDirection: "row", flexWrap: "wrap", gap: Spacing.xs, marginBottom: Spacing.md },
  quickChip:     { paddingHorizontal: 14, paddingVertical: 7, borderRadius: Radius.pill, borderWidth: 1, borderColor: Colors.border, backgroundColor: Colors.card },
  quickChipActive:{ borderColor: Colors.gold, backgroundColor: `${Colors.gold}10` },
  quickText:     { fontSize: FontSize.sm, color: Colors.t2 },

  input: {
    backgroundColor: Colors.card, borderRadius: Radius.md, borderWidth: 1,
    borderColor: Colors.border, padding: Spacing.md,
    fontSize: FontSize.lg, fontWeight: "700", color: Colors.t1, marginBottom: Spacing.md,
  },

  btn:         { backgroundColor: Colors.gold, borderRadius: Radius.pill, padding: Spacing.md, alignItems: "center" },
  btnDisabled: { backgroundColor: Colors.border, opacity: 0.6 },
  btnText:     { color: "#fff", fontSize: FontSize.md, fontWeight: "700" },

  note:    { fontSize: FontSize.xs, color: Colors.t3, textAlign: "center", marginTop: Spacing.sm },

  doneTitle: { fontSize: FontSize.xxl, fontWeight: "700", color: Colors.t1, marginTop: Spacing.lg, textAlign: "center" },
  doneSub:   { fontSize: FontSize.base, color: Colors.t2, textAlign: "center", marginTop: Spacing.xs },
});
