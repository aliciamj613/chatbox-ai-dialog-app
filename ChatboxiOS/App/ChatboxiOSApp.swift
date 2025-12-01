import SwiftUI

@main
struct ChatboxIOSApp: App {
    // 不用 @StateObject，直接常量即可（在 App 里生命周期就是全局的）
    let appState = AppState()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(appState)
                .preferredColorScheme(appState.isDarkMode ? .dark : .light)
        }
    }
}
