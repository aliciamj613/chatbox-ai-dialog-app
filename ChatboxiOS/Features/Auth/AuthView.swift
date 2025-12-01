import SwiftUI

struct AuthView: View {
    @EnvironmentObject var appState: AppState
    @StateObject private var vm = AuthViewModel()

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                Text(titleText)
                    .font(.title2)
                    .bold()

                Spacer().frame(height: 8)

                switch vm.mode {
                case .login:
                    loginForm
                case .register:
                    registerForm
                case .reset:
                    resetForm
                }

                if let info = vm.info {
                    Text(info)
                        .foregroundColor(.green)
                        .font(.footnote)
                }

                if let err = vm.error {
                    Text(err)
                        .foregroundColor(.red)
                        .font(.footnote)
                }

                Spacer()

                footerButtons
            }
            .padding()
        }
    }

    private var titleText: String {
        switch vm.mode {
        case .login: return "登录"
        case .register: return "注册新账号"
        case .reset: return "重置密码"
        }
    }

    // MARK: - Forms

    private var loginForm: some View {
        VStack(alignment: .leading, spacing: 12) {
            TextField("用户名", text: $vm.loginName)
                .textFieldStyle(.roundedBorder)
                .textInputAutocapitalization(.never)

            PasswordField(title: "密码", text: $vm.loginPassword)

            Button {
                vm.login(appState: appState)
            } label: {
                if vm.isLoggingIn {
                    ProgressView()
                } else {
                    Text("登录")
                        .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .padding(.top, 4)
        }
    }

    private var registerForm: some View {
        VStack(alignment: .leading, spacing: 12) {
            TextField("用户名", text: $vm.registerName)
                .textFieldStyle(.roundedBorder)
                .textInputAutocapitalization(.never)

            SecureField("密码", text: $vm.registerPassword)
                .textFieldStyle(.roundedBorder)

            SecureField("确认密码", text: $vm.registerConfirm)
                .textFieldStyle(.roundedBorder)

            Button {
                vm.register(appState: appState)
            } label: {
                if vm.isRegistering {
                    ProgressView()
                } else {
                    Text("注册")
                        .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .padding(.top, 4)
        }
    }

    private var resetForm: some View {
        VStack(alignment: .leading, spacing: 12) {
            TextField("用户名", text: $vm.resetName)
                .textFieldStyle(.roundedBorder)
                .textInputAutocapitalization(.never)

            SecureField("新密码", text: $vm.resetNewPassword)
                .textFieldStyle(.roundedBorder)

            SecureField("确认密码", text: $vm.resetConfirm)
                .textFieldStyle(.roundedBorder)

            Button {
                vm.resetPassword(appState: appState)
            } label: {
                if vm.isResetting {
                    ProgressView()
                } else {
                    Text("重置密码")
                        .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .padding(.top, 4)
        }
    }

    // MARK: - Footer

    private var footerButtons: some View {
        VStack(spacing: 8) {
            switch vm.mode {
            case .login:
                HStack {
                    Button("还没有账号？去注册") {
                        vm.switchMode(.register)
                    }
                    Spacer()
                    Button("忘记密码？") {
                        vm.switchMode(.reset)
                    }
                }
                .font(.footnote)

            case .register, .reset:
                Button("返回登录") {
                    vm.switchMode(.login)
                }
                .font(.footnote)
            }
        }
    }
}
