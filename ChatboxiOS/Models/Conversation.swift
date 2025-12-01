import Foundation

struct Conversation: Identifiable, Codable, Equatable {
    let id: Int64
    let userId: Int64
    var title: String
    var createdAt: Date
    var updatedAt: Date
}
