import SwiftUI

struct ContentView: View {
    @EnvironmentObject var appState: AppState

    var body: some View {
        if appState.currentUser == nil {
            AuthView()
        } else {
            ConversationListView()
        }
    }
}
