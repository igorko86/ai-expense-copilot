import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './context/AuthContext';
import App from './App.tsx';

// Створюємо екземпляр клієнта для кешування серверних даних
const queryClient = new QueryClient();

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthProvider onLogout={() => queryClient.clear()}>
        <App />
      </AuthProvider>
    </QueryClientProvider>
  </StrictMode>
);
