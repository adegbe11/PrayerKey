import {
  View, Text, TextInput, TouchableOpacity,
  StyleSheet, ScrollView, KeyboardAvoidingView, Platform, ActivityIndicator,
} from "react-native";
import { useState } from "react";
import { router, Link } from "expo-router";
import { useAuth } from "@/hooks/useAuth";
import { login } from "@/services/api";
import { Colors, Spacing, Radius, FontSize } from "@/constants/theme";

export default function LoginScreen() {
  const { signIn } = useAuth();
  const [email,    setEmail]    = useState("");
  const [password, setPassword] = useState("");
  const [loading,  setLoading]  = useState(false);
  const [error,    setError]    = useState("");

  async function handleLogin() {
    if (!email.trim() || !password) return;
    setLoading(true);
    setError("");
    try {
      const res = await login(email.trim(), password);
      await signIn(res);
      router.replace("/(tabs)");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <KeyboardAvoidingView
      style={styles.root}
      behavior={Platform.OS === "ios" ? "padding" : undefined}
    >
      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">

        {/* Logo mark */}
        <View style={styles.logoWrap}>
          <View style={styles.cross}>
            <View style={styles.crossV} />
            <View style={styles.crossH} />
          </View>
        </View>

        <Text style={styles.brand}>PrayerKey</Text>
        <Text style={styles.sub}>Sign in to your church account</Text>

        {/* Form */}
        <View style={styles.form}>
          <TextInput
            style={styles.input}
            placeholder="Email"
            placeholderTextColor={Colors.t3}
            keyboardType="email-address"
            autoCapitalize="none"
            value={email}
            onChangeText={setEmail}
          />
          <TextInput
            style={styles.input}
            placeholder="Password"
            placeholderTextColor={Colors.t3}
            secureTextEntry
            value={password}
            onChangeText={setPassword}
          />

          {error ? <Text style={styles.error}>{error}</Text> : null}

          <TouchableOpacity
            style={[styles.btn, (!email || !password || loading) && styles.btnDisabled]}
            onPress={handleLogin}
            disabled={!email || !password || loading}
            activeOpacity={0.8}
          >
            {loading
              ? <ActivityIndicator color="#fff" />
              : <Text style={styles.btnText}>Sign In</Text>
            }
          </TouchableOpacity>
        </View>

        <View style={styles.footer}>
          <Text style={styles.footerText}>Don&apos;t have an account? </Text>
          <Link href="/(auth)/register" asChild>
            <TouchableOpacity>
              <Text style={styles.footerLink}>Register</Text>
            </TouchableOpacity>
          </Link>
        </View>

      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  root:      { flex: 1, backgroundColor: Colors.bg },
  scroll:    { flexGrow: 1, justifyContent: "center", padding: Spacing.lg },

  logoWrap:  { alignItems: "center", marginBottom: Spacing.md },
  cross:     { width: 56, height: 56, alignItems: "center", justifyContent: "center" },
  crossV:    { position: "absolute", width: 8, height: 48, borderRadius: 4, backgroundColor: Colors.gold },
  crossH:    { position: "absolute", top: 12, width: 40, height: 8, borderRadius: 4, backgroundColor: Colors.gold },

  brand:     { fontSize: 28, fontWeight: "700", color: Colors.t1, textAlign: "center", marginBottom: 6 },
  sub:       { fontSize: FontSize.base, color: Colors.t2, textAlign: "center", marginBottom: Spacing.xl },

  form:      { gap: Spacing.sm },
  input:     {
    backgroundColor: Colors.card,
    borderRadius:    Radius.md,
    borderWidth:     1,
    borderColor:     Colors.border,
    padding:         Spacing.md,
    fontSize:        FontSize.md,
    color:           Colors.t1,
  },

  error:     { fontSize: FontSize.sm, color: Colors.red, textAlign: "center" },

  btn:       {
    backgroundColor: Colors.t1,
    borderRadius:    Radius.pill,
    padding:         Spacing.md,
    alignItems:      "center",
    marginTop:       Spacing.xs,
  },
  btnDisabled: { backgroundColor: Colors.border, opacity: 0.6 },
  btnText:     { color: "#fff", fontSize: FontSize.md, fontWeight: "700" },

  footer:     { flexDirection: "row", justifyContent: "center", marginTop: Spacing.xl },
  footerText: { color: Colors.t2, fontSize: FontSize.sm },
  footerLink: { color: Colors.gold, fontSize: FontSize.sm, fontWeight: "600" },
});
