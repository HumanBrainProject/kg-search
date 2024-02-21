import react from '@vitejs/plugin-react'
import viteTsconfigPaths from 'vite-tsconfig-paths'

export default {
    // depending on your application, base can also be "/"
    base: '/',
    plugins: [react(), viteTsconfigPaths()],
    server: {
        // this ensures that the browser opens upon server start
        open: true,
        // this sets a default port to 3000
        port: 3000,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                // target: 'https://search.kg.ebrains.eu',
                //target: 'https://search.kg-int.ebrains.eu',
                // target: 'https://search.kg-ppd.ebrains.eu',
                // target: 'https://search.kg-dev.ebrains.eu',
                changeOrigin: true
            }
        }
    },
}