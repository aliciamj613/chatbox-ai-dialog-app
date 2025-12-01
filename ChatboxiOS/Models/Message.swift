import Foundation

struct Message: Identifiable, Codable, Equatable {
    let id: Int64
    var text: String
    var isUser: Bool
    var timestamp: Date
    var conversationId: Int64
}
