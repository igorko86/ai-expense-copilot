const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const API_BASE = `${API_BASE_URL}/api`;
const EXPENSES = `${API_BASE}/ai/expenses`;
const DOCUMENTS = `${API_BASE}/ai/documents`;

export const TOKEN_KEY = 'expense_copilot_token';
export const EMAIL_KEY = 'expense_copilot_email';

export interface Expense {
  id: number;
  description: string;
  amount: number;
  currency: string;
  category: string;
  expenseDate: string;
}

export function getAuthHeaders(): HeadersInit {
  const token = localStorage.getItem(TOKEN_KEY);
  return token ? { Authorization: `Bearer ${token}` } : {};
}

// Централізована обгортка з автоматичним підставленням JWT та обробкою 401/403
export async function apiFetch(url: string, options: RequestInit = {}): Promise<Response> {
  const isFormData = options.body instanceof FormData;

  const headers: HeadersInit = {
    ...getAuthHeaders(),
    ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
    ...options.headers,
  };

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (response.status === 401 || response.status === 403) {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(EMAIL_KEY);
    window.dispatchEvent(new Event('auth:logout'));
    throw new Error('Unauthorized');
  }

  return response;
}

// --- Ендпоінти авторизації ---

export async function loginUser(email: string, password: string): Promise<{ token: string; email: string }> {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });
  if (!res.ok) throw new Error('Невірний email або пароль');
  return res.json();
}

export async function registerUser(email: string, password: string): Promise<{ token: string; email: string }> {
  const res = await fetch(`${API_BASE}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });
  if (!res.ok) throw new Error('Помилка реєстрації (можливо, email вже зайнятий)');
  return res.json();
}

// --- Ендпоінти витрат та AI ---

export async function fetchExpenses(): Promise<Expense[]> {
  const res = await apiFetch(EXPENSES);
  if (!res.ok) throw new Error('Failed to fetch expenses');
  return res.json();
}

export async function parseAndSaveExpense(text: string): Promise<Expense[]> {
  const res = await apiFetch(`${EXPENSES}/parse`, {
    method: 'POST',
    body: JSON.stringify({ text }),
  });
  if (!res.ok) throw new Error('Failed to parse expense');
  return res.json();
}

export async function askAiAssistant(question: string): Promise<string> {
  const res = await apiFetch(`${EXPENSES}/chat`, {
    method: 'POST',
    body: JSON.stringify({ question }),
  });
  if (!res.ok) throw new Error('Failed to get AI answer');
  const data = await res.json();
  return data.answer;
}

export async function uploadPdfDocument(file: File): Promise<string> {
  const formData = new FormData();
  formData.append('file', file);

  const res = await apiFetch(`${DOCUMENTS}/upload-pdf`, {
    method: 'POST',
    body: formData,
  });
  if (!res.ok) throw new Error('Failed to upload PDF');
  return res.text();
}

// Зберігаємо твою сигнатуру для App.tsx, але використовуємо fetch замість EventSource (щоб передати JWT)
export async function streamAiAssistant(
  question: string,
  onChunk: (chunk: string) => void,
  onDone: () => void,
  onError: () => void
) {
  try {
    const params = new URLSearchParams({ question });
    const response = await apiFetch(`${EXPENSES}/chat/stream?${params.toString()}`, {
      method: 'GET',
      headers: {
        Accept: 'text/event-stream',
      },
    });

    if (!response.ok || !response.body) {
      throw new Error(`Stream error: ${response.status}`);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let buffer = '';

    while (true) {
      const { value, done } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split('\n');
      buffer = lines.pop() ?? '';

      for (const line of lines) {
        if (line.startsWith('data:')) {
          const content = line.slice(5);
          onChunk(content);
        }
      }
    }

    onDone();
  } catch (err) {
    console.error('Stream failed:', err);
    onError();
  }
}
