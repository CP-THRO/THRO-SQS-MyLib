// config.ts

export interface Config {
    BACKEND_HOST: string;
    BACKEND_PORT: string;
    BACKEND_PROTO: string;
}

export const config: Config = {
    BACKEND_HOST: 'localhost',
    BACKEND_PORT: "8080",
    BACKEND_PROTO : "http",
};

export async function loadAppConfig(): Promise<void> {
    try {
        const response = await fetch('/config.json');
        if (!response.ok) {
            throw new Error('Failed to fetch config.json');
        }
        const data = await response.json();

        config.BACKEND_HOST = data.BACKEND_HOST ?? 'localhost';
        config.BACKEND_PORT = data.BACKEND_PORT ?? "8080"
        config.BACKEND_PROTO = data.BACKEND_PROTO ?? "http"
    } catch (error) {
        console.error(error);
        config.BACKEND_HOST= 'localhost';
        config.BACKEND_PORT= "8080"
        config.BACKEND_PROTO = "http"
    }
}
