import {
  View, Text, TextInput, TouchableOpacity,
  StyleSheet, ScrollView, KeyboardAvoidingView, Platform, ActivityIndicator,
} from "react-native";
import { useState } from "react";
import { router, Link } from "expo-router";
import { useAuth } from "@/hooks/useAuth";
import { register } from "@/services/api";
import { Colors, Spacing, Radius, FontSize } from "@/constants/theme";

export default function RegisterScreen() {
  const { signIn } = useAuth();
  const [form, setForm] = useState({ name: "", email: "", password: "", churchCode: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState("");

  function update(field: string, value: string) {
    setForm((p) => ({ ...p, [field]: value }));
  }

  async function handleRegister() {
    if (!form.name || !form.email || !form.password) return;
    setLoading(true);
    setError("");
    try {
      const res = await register({
        name:       form.name.trim(),
        email:      form.email.trim(),
        password:   form.password,
        churchCode: form.churchCode.trim() || undefined,
      });
      await signIn(res);
      router.replace("/(tabs)");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Registration failed");
    } finally {
      setLoading(false);
    }
  }

  const disabled = !form.name || !form.email || !form.password || loading;

  return (
    <KeyboardAvoidingView
      style={styles.root}
      behavior={Platform.OS === "ios" ? "padding" : undefined}
    >
      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">

        <View style={styles.logoWrap}>
          <View style={styles.cross}>
            <View style={styles.crossV} />
            <View style={styles.crossH} />
          </View>
        </View>

        <Text style={styles.brand}>Create Account</Text>
        <Text style={styles.sub}>Join your church on PrayerKey</Text>

        <View style={styles.form}>
          <TextInput
            style={styles.input}
            placeholder="Full name"
            placeholderTextColor={Colors.t3}
            value={form.name}
            onChangeText={(v) => update("name", v)}
          />
          <TextInput
            style={styles.input}
            placeholder="Email"
            placeholderTextColor={Colors.t3}
            keyboardType="email-address"
            autoCapitalize="none"
            value={form.email}
            onChangeText={(v) => update("email", v)}
          />
          <TextInput
            style={styles.input}
            placeholder="Password"
            placeholderTextColor={Colors.t3}
            secureTextEntry
            value={form.password}
            onChangeText={(v) => update("password", v)}
          />
          <TextInput
            style={styles.input}
            placeholder="Church code (optional)"
            placeholderTextColor={Colors.t3}
            autoCapitalize="none"
            value={form.churchCode}
            onChangeText={(v) => update("churchCode", v)}
          />
          <Text style={styles.hint}>
            Ask your church admin for your church&apos;s unique code to join their community.
          </Text>

          {error ? <Text style={styles.error}>{error}</Text> : null}

          <TouchableOpacity
            style={[styles.btn, disabled && styles.btnDisabled]}
            onPress={handleRegister}
            disabled={disabled}
            activeOpacity={0.8}
          >
            {loading
              ? <ActivityIndicator color="#fff" />
              : <Text style={styles.btnText}>Create Account</Text>
            }
          </TouchableOpacity>
        </View>

        <View style={styles.footer}>
          <Text style={styles.footerText}>Already have an account? </Text>
          <Link href="/(auth)/login" asChild>
            <TouchableOpacity>
              <Text style={styles.footerLink}>Sign In</Text>
            </TouchableOpacity>
          </Link>
        </View>

      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  root:       { flex: 1, backgroundColor: Colors.bg },
  scroll:     { flexGrow: 1, justifyContent: "center", padding: Spacing.lg },
  logoWrap:   { alignItems: "center", marginBottom: Spacing.md },
  cross:      { width: 48, height: 48, alignItems: "center", justifyContent: "center" },
  crossV:     { position: "absolute", width: 6, height: 40, borderRadius: 3, backgroundColor: Colors.gold },
  crossH:     { position: "absolute", top: 10, width: 34, height: 6, borderRadius: 3, backgroundColor: Colors.gold },
  brand:      { fontSize: 26, fontWeight: "700", color: Colors.t1, textAlign: "center", marginBottom: 6 },
  sub:        { fontSize: FontSize.base, color: Colors.t2, textAlign: "center", marginBottom: Spacing.xl },
  form:       { gap: Spacing.sm },
  input:      {
    backgroundColor: Colors.card, borderRadius: Radius.md, borderWidth: 1,
    borderColor: Colors.border, padding: Spacing.md, fontSize: FontSize.md, color: Colors.t1,
  },
  hint:       { fontSize: FontSize.xs, color: Colors.t3, paddingHorizontal: 4 },
  error:      { fontSize: FontSize.sm, color: Colors.red, textAlign: "center" },
  btn:        { backgroundColor: Colors.t1, borderRadius: Radius.pill, padding: Spacing.md, alignItems: "center", marginTop: Spacing.xs },
  btnDisabled: { backgroundColor: Colors.border, opacity: 0.6 },
  btnText:    { color: "#fff", fontSize: FontSize.md, fontWeight: "700" },
  footer:     { flexDirection: "row", justifyContent: "center", marginTop: Spacing.xl },
  footerText: { color: Colors.t2, fontSize: FontSize.sm },
  footerLink: { color: Colors.gold, fontSize: FontSize.sm, fontWeight: "600" },
});
