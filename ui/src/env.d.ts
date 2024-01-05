/// <reference types="vite/client" />

interface ImportMetaEnv {
    readonly VITE_APP_BYPASS_AUTH: string
    readonly MODE: string
}

interface ImportMeta {
    readonly env: ImportMetaEnv
}
