import Foundation
import Combine 
enum AuthMode {
    case login
    case register
    case reset
}

final class AuthViewModel: ObservableObject {
    @Published var mode: AuthMode = .login

    // 登录
    @Published var loginName: String = ""
    @Published var loginPassword: String = ""
    @Published var isLoggingIn = false

    // 注册
    @Published var registerName: String = ""
    @Published var registerPassword: String = ""
    @Published var registerConfirm: String = ""
    @Published var isRegistering = false

    // 重置
    @Published var resetName: String = ""
    @Published var resetNewPassword: String = ""
    @Published var resetConfirm: String = ""
    @Published var isResetting = false

    @Published var error: String?
    @Published var info: String?

    func switchMode(_ newMode: AuthMode) {
        mode = newMode
        error = nil
        info = nil
    }

    func login(appState: AppState) {
        let name = loginName.trimmingCharacters(in: .whitespaces)
        let pwd = loginPassword

        guard !name.isEmpty, !pwd.isEmpty else {
            error = "用户名和密码不能为空"
            return
        }

        isLoggingIn = true
        error = nil
        info = nil

        DispatchQueue.global().async {
            let user = appState.store.login(name: name, password: pwd)
            DispatchQueue.main.async {
                self.isLoggingIn = false
                if let u = user {
                    appState.currentUser = u
                } else {
                    self.error = "用户名或密码错误"
                }
            }
        }
    }

    func register(appState: AppState) {
        let name = registerName.trimmingCharacters(in: .whitespaces)
        let pwd = registerPassword
        let confirm = registerConfirm

        guard !name.isEmpty, !pwd.isEmpty else {
            error = "用户名和密码不能为空"
            return
        }
        guard pwd == confirm else {
            error = "两次密码不一致"
            return
        }

        isRegistering = true
        error = nil
        info = nil

        DispatchQueue.global().async {
            do {
                _ = try appState.store.register(name: name, password: pwd)
                DispatchQueue.main.async {
                    self.isRegistering = false
                    self.info = "注册成功，请返回登录"
                    self.registerPassword = ""
                    self.registerConfirm = ""
                }
            } catch {
                DispatchQueue.main.async {
                    self.isRegistering = false
                    self.error = error.localizedDescription
                }
            }
        }
    }

    func resetPassword(appState: AppState) {
        let name = resetName.trimmingCharacters(in: .whitespaces)
        let pwd = resetNewPassword
        let confirm = resetConfirm

        guard !name.isEmpty, !pwd.isEmpty else {
            error = "用户名和密码不能为空"
            return
        }
        guard pwd == confirm else {
            error = "两次密码不一致"
            return
        }

        isResetting = true
        error = nil
        info = nil

        DispatchQueue.global().async {
            do {
                try appState.store.resetPassword(name: name, newPassword: pwd)
                DispatchQueue.main.async {
                    self.isResetting = false
                    self.info = "密码重置成功，请使用新密码登录"
                }
            } catch {
                DispatchQueue.main.async {
                    self.isResetting = false
                    self.error = error.localizedDescription
                }
            }
        }
    }
}
