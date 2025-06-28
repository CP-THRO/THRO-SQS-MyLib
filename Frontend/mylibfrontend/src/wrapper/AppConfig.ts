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
        console.log(data)
        console.log(data.BACKEND_HOST)
        console.log(data.BACKEND_PORT)

        config.BACKEND_HOST = data.BACKEND_HOST ?? 'http://localhost';
        config.BACKEND_PORT = data.BACKEND_PORT ?? "8080"
        console.log(config.BACKEND_HOST)
        console.log(config.BACKEND_PORT)
    } catch (error) {
        console.error(error);
        config.BACKEND_HOST= 'http://localhost';
        config.BACKEND_PORT= "8080"
    }
}
