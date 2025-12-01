import Foundation

struct User: Identifiable, Codable, Equatable {
    let id: Int64
    var name: String
    var password: String
}
