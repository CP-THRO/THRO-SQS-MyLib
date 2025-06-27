// config.ts

export interface Config {
    BACKEND_HOST: string;
    BACKEND_PORT: string;
}

export const config: Config = {
    BACKEND_HOST: 'http://localhost',
    BACKEND_PORT: "8080"
};

export async function loadAppConfig(): Promise<void> {
    try {
        const response = await fetch('/config.json');
        if (!response.ok) {
            throw new Error('Failed to fetch config.json');
        }
        const data = await response.json();
        config.BACKEND_HOST = data.BACKEND_BASE_URL ?? 'http://localhost';
        config.BACKEND_PORT = data.BACKEND_PORT ?? "7070"
    } catch (error) {
        console.error(error);
        config.BACKEND_HOST= 'http://localhost';
        config.BACKEND_PORT= "8080"
    }
}
