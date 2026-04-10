import { Tabs, Redirect } from "expo-router";
import { useAuth } from "@/hooks/useAuth";
import { View, ActivityIndicator } from "react-native";
import { Colors } from "@/constants/theme";
import { Ionicons } from "@expo/vector-icons";

type IoniconsName = React.ComponentProps<typeof Ionicons>["name"];

function TabIcon({ name, focused }: { name: IoniconsName; focused: boolean }) {
  return (
    <Ionicons
      name={name}
      size={24}
      color={focused ? Colors.gold : Colors.t3}
    />
  );
}

export default function TabLayout() {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <View style={{ flex: 1, justifyContent: "center", alignItems: "center", backgroundColor: Colors.bg }}>
        <ActivityIndicator color={Colors.gold} />
      </View>
    );
  }

  if (!user) return <Redirect href="/(auth)/login" />;

  return (
    <Tabs
      screenOptions={{
        headerShown:     false,
        tabBarActiveTintColor:   Colors.gold,
        tabBarInactiveTintColor: Colors.t3,
        tabBarStyle: {
          backgroundColor: Colors.card,
          borderTopColor:  Colors.border,
          borderTopWidth:  0.5,
          paddingBottom:   4,
          height:          60,
        },
        tabBarLabelStyle: {
          fontSize:   10,
          fontWeight: "600",
          marginTop:  -2,
        },
      }}
    >
      <Tabs.Screen
        name="index"
        options={{
          title:    "Home",
          tabBarIcon: ({ focused }) => <TabIcon name={focused ? "home" : "home-outline"} focused={focused} />,
        }}
      />
      <Tabs.Screen
        name="pray"
        options={{
          title:    "Pray",
          tabBarIcon: ({ focused }) => <TabIcon name={focused ? "sparkles" : "sparkles-outline"} focused={focused} />,
        }}
      />
      <Tabs.Screen
        name="live"
        options={{
          title:    "Live",
          tabBarIcon: ({ focused }) => (
            <View
              style={{
                width:           44,
                height:          44,
                borderRadius:    22,
                backgroundColor: focused ? Colors.red : `${Colors.red}22`,
                justifyContent:  "center",
                alignItems:      "center",
                marginBottom:    8,
              }}
            >
              <Ionicons name="radio" size={22} color={focused ? "#fff" : Colors.red} />
            </View>
          ),
        }}
      />
      <Tabs.Screen
        name="community"
        options={{
          title:    "Community",
          tabBarIcon: ({ focused }) => <TabIcon name={focused ? "people" : "people-outline"} focused={focused} />,
        }}
      />
      <Tabs.Screen
        name="give"
        options={{
          title:    "Give",
          tabBarIcon: ({ focused }) => <TabIcon name={focused ? "heart" : "heart-outline"} focused={focused} />,
        }}
      />
      <Tabs.Screen
        name="profile"
        options={{
          title:    "Profile",
          tabBarIcon: ({ focused }) => <TabIcon name={focused ? "person-circle" : "person-circle-outline"} focused={focused} />,
        }}
      />
    </Tabs>
  );
}
