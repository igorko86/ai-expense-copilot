import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';

export function AuthForm() {
  const { login, register } = useAuth();
  const [isRegister, setIsRegister] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      if (isRegister) {
        await register(email, password);
      } else {
        await login(email, password);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Помилка авторизації');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 400, margin: '80px auto', padding: 24, border: '1px solid #e5e7eb', borderRadius: 12 }}>
      <h2 style={{ marginBottom: 16 }}>
        {isRegister ? 'Реєстрація в AI Expense Copilot' : 'Вхід в AI Expense Copilot'}
      </h2>

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          style={{ padding: 10, borderRadius: 6, border: '1px solid #ccc' }}
        />
        <input
          type="password"
          placeholder="Пароль"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          minLength={6}
          style={{ padding: 10, borderRadius: 6, border: '1px solid #ccc' }}
        />

        {error && <div style={{ color: '#dc2626', fontSize: 14 }}>{error}</div>}

        <button
          type="submit"
          disabled={loading}
          style={{ padding: 10, borderRadius: 6, background: '#2563eb', color: '#fff', border: 'none', cursor: 'pointer' }}
        >
          {loading ? 'Завантаження...' : isRegister ? 'Зареєструватися' : 'Увійти'}
        </button>
      </form>

      <button
        type="button"
        onClick={() => { setIsRegister(!isRegister); setError(null); }}
        style={{ marginTop: 16, background: 'none', border: 'none', color: '#2563eb', cursor: 'pointer', textDecoration: 'underline' }}
      >
        {isRegister ? 'Вже є акаунт? Увійти' : 'Немає акаунта? Зареєструватися'}
      </button>
    </div>
  );
}
