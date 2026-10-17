import AVFAudio
import PrayerKeyUI
import UIKit
import UserNotifications

/// Starts the shared Kotlin app and hands it the few things only iOS can:
/// the audio session, notification taps, and the status bar's style.
@main
final class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // Read-aloud and the soundscapes play with the ring switch on silent,
        // as a reading app's audio should.
        try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .spokenAudio)
        UNUserNotificationCenter.current().delegate = self

        let window = UIWindow(frame: UIScreen.main.bounds)
        window.rootViewController = HostController()
        window.makeKeyAndVisible()
        self.window = window
        return true
    }

    /// A reminder that arrives while the app is open still shows.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound])
    }

    /// A tapped reminder opens where it points: the prayer, the reading, the fast.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        if let route = response.notification.request.content.userInfo["prayerkey_destination"] as? String {
            NotificationRoutes.shared.open(route: route)
        }
        completionHandler()
    }
}

/// Hosts the Compose screens and answers iOS's question about the status bar
/// with what the current screen asked for (light icons on the dark Bible tab).
final class HostController: UIViewController {
    private let compose = MainViewControllerKt.MainViewController()

    override func viewDidLoad() {
        super.viewDidLoad()
        addChild(compose)
        compose.view.frame = view.bounds
        compose.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(compose.view)
        compose.didMove(toParent: self)
        StatusBar.shared.onChange = { [weak self] in
            self?.setNeedsStatusBarAppearanceUpdate()
        }
    }

    override var preferredStatusBarStyle: UIStatusBarStyle {
        StatusBar.shared.darkIcons ? .darkContent : .lightContent
    }
}
