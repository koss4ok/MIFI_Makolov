const el = (id) => document.getElementById(id);

const output = el("output");
const baseUrlInput = el("baseUrl");
const tokenInput = el("token");

const storedToken = localStorage.getItem("makolovOtpToken");
if (storedToken) {
    tokenInput.value = storedToken;
}

function logResult(title, status, body) {
    output.textContent = `${title}\nstatus: ${status}\n\n${JSON.stringify(body, null, 2)}`;
}

function getBaseUrl() {
    return (baseUrlInput.value || "http://localhost:8080").replace(/\/$/, "");
}

function getHeaders(withAuth = false) {
    const headers = { "Content-Type": "application/json" };
    if (withAuth) {
        const token = tokenInput.value.trim();
        if (token) {
            headers["Authorization"] = `Bearer ${token}`;
        }
    }
    return headers;
}

async function sendRequest({ title, method, path, body, auth }) {
    const url = getBaseUrl() + path;
    try {
        const response = await fetch(url, {
            method,
            headers: getHeaders(auth),
            body: body ? JSON.stringify(body) : undefined
        });

        let payload;
        try {
            payload = await response.json();
        } catch (e) {
            payload = { message: "Не удалось распарсить JSON", rawStatus: response.status };
        }

        if (title === "Логин" && payload?.data?.token) {
            tokenInput.value = payload.data.token;
            localStorage.setItem("makolovOtpToken", payload.data.token);
        }

        logResult(title, response.status, payload);
    } catch (err) {
        logResult(title, "network_error", { error: String(err) });
    }
}

el("saveToken").addEventListener("click", () => {
    localStorage.setItem("makolovOtpToken", tokenInput.value.trim());
    logResult("Токен", 200, { saved: true });
});

el("clearToken").addEventListener("click", () => {
    tokenInput.value = "";
    localStorage.removeItem("makolovOtpToken");
    logResult("Токен", 200, { cleared: true });
});

el("registerBtn").addEventListener("click", async () => {
    await sendRequest({
        title: "Регистрация",
        method: "POST",
        path: "/api/auth/register",
        body: {
            login: el("regLogin").value,
            password: el("regPassword").value,
            role: el("regRole").value
        },
        auth: false
    });
});

el("loginBtn").addEventListener("click", async () => {
    await sendRequest({
        title: "Логин",
        method: "POST",
        path: "/api/auth/login",
        body: {
            login: el("login").value,
            password: el("password").value
        },
        auth: false
    });
});

el("generateBtn").addEventListener("click", async () => {
    await sendRequest({
        title: "Генерация OTP",
        method: "POST",
        path: "/api/user/otp/generate",
        body: {
            operationId: el("genOperationId").value,
            channel: el("genChannel").value,
            destination: el("genDestination").value
        },
        auth: true
    });
});

el("validateBtn").addEventListener("click", async () => {
    await sendRequest({
        title: "Валидация OTP",
        method: "POST",
        path: "/api/user/otp/validate",
        body: {
            operationId: el("valOperationId").value,
            code: el("valCode").value
        },
        auth: true
    });
});

el("updateConfigBtn").addEventListener("click", async () => {
    await sendRequest({
        title: "Обновление конфигурации",
        method: "PUT",
        path: "/api/admin/config",
        body: {
            codeLength: Number(el("cfgLength").value),
            ttlSeconds: Number(el("cfgTtl").value)
        },
        auth: true
    });
});

el("listUsersBtn").addEventListener("click", async () => {
    await sendRequest({
        title: "Список пользователей",
        method: "GET",
        path: "/api/admin/users",
        auth: true
    });
});

el("deleteUserBtn").addEventListener("click", async () => {
    const userId = Number(el("deleteUserId").value);
    await sendRequest({
        title: "Удаление пользователя",
        method: "DELETE",
        path: `/api/admin/users/${userId}`,
        auth: true
    });
});
