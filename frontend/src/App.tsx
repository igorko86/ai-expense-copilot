import { useEffect, useState, useMemo } from 'react';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { Send, PlusCircle, FileUp, Bot, Wallet } from 'lucide-react';
import {
 type Expense,
  fetchExpenses,
  parseAndSaveExpense,
  askAiAssistant,
  streamAiAssistant,
  uploadPdfDocument,
} from './api';
import './App.css';

const COLORS = ['#2563eb', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'];

export function App() {
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [rawText, setRawText] = useState('');
  const [isParsing, setIsParsing] = useState(false);

  const [messages, setMessages] = useState<{ role: 'user' | 'ai'; text: string }[]>([
    { role: 'ai', text: 'Привіт! Я твій фінансовий AI-асистент. Запитай мене про свої витрати або завантаж PDF-документ.' },
  ]);
  const [question, setQuestion] = useState('');
  const [isChatLoading, setIsChatLoading] = useState(false);
  const [uploadStatus, setUploadStatus] = useState('');

  useEffect(() => {
    loadExpenses();
  }, []);

  async function loadExpenses() {
    try {
      const data = await fetchExpenses();
      setExpenses(data);
    } catch (err) {
      console.error(err);
    }
  }

  // Агрегуємо витрати по категоріях для графіка Recharts
  const chartData = useMemo(() => {
    const map = new Map<string, number>();
    expenses.forEach((e) => {
      map.set(e.category, (map.get(e.category) || 0) + Number(e.amount));
    });
    return Array.from(map.entries()).map(([name, value]) => ({ name, value }));
  }, [expenses]);

  async function handleAddExpense(e: React.FormEvent) {
    e.preventDefault();
    if (!rawText.trim()) return;
    setIsParsing(true);
    try {
      const created = await parseAndSaveExpense(rawText);
      setExpenses((prev) => [created, ...prev]);
      setRawText('');
    } catch {
      alert('Помилка парсингу витрати. Перевір, чи запущений Java-бекенд.');
    } finally {
      setIsParsing(false);
    }
  }

  async function handleSendQuestion(e: React.FormEvent) {
    e.preventDefault();
    if (!question.trim()) return;
    const userMsg = question;
    setQuestion('');
    setIsChatLoading(true);

    // Додаємо питання користувача та порожню бульбашку для відповіді AI
    setMessages((prev) => [
      ...prev,
      { role: 'user', text: userMsg },
      { role: 'ai', text: '' },
    ]);

    streamAiAssistant(
      userMsg,
      (chunk) => {
        // Дописуємо кожен новий токен до останнього повідомлення AI
        setMessages((prev) => {
          const updated = [...prev];
          const lastIdx = updated.length - 1;
          updated[lastIdx] = {
            ...updated[lastIdx],
            text: updated[lastIdx].text + chunk,
          };
          return updated;
        });
      },
      () => setIsChatLoading(false),
      () => setIsChatLoading(false)
    );
}

  async function handlePdfUpload(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploadStatus('Завантаження та векторизація PDF...');
    try {
      const res = await uploadPdfDocument(file);
      setUploadStatus(res);
    } catch {
      setUploadStatus('Помилка завантаження PDF.');
    }
  }

  return (
    <div className="container">
      <header className="header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Wallet size={28} color="#2563eb" />
          <h1>AI Expense Copilot</h1>
        </div>
        <div>
          <label style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px', color: '#2563eb', fontWeight: 600 }}>
            <FileUp size={18} />
            Завантажити PDF (RAG)
            <input type="file" accept="application/pdf" hidden onChange={handlePdfUpload} />
          </label>
          {uploadStatus && <div style={{ fontSize: '12px', color: '#10b981', marginTop: '4px' }}>{uploadStatus}</div>}
        </div>
      </header>

      <div className="grid">
        {/* Ліва колонка: Додавання витрат, Графік та Список */}
        <div>
          <div className="card">
            <h2><PlusCircle size={20} color="#2563eb" /> Додати витрату (AI Парсер)</h2>
            <form onSubmit={handleAddExpense} className="input-row">
              <input
                type="text"
                placeholder="Наприклад: Кава та круасан 165 грн сьогодні"
                value={rawText}
                onChange={(e) => setRawText(e.target.value)}
                disabled={isParsing}
              />
              <button type="submit" disabled={isParsing}>
                {isParsing ? 'Аналіз...' : 'Додати'}
              </button>
            </form>
          </div>

          <div className="card">
            <h2>Розподіл витрат за категоріями</h2>
            {chartData.length > 0 ? (
              <div style={{ width: '100%', height: 240 }}>
                <ResponsiveContainer>
                  <PieChart>
                    <Pie data={chartData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
                      {chartData.map((_, idx) => (
                        <Cell key={idx} fill={COLORS[idx % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            ) : (
              <p style={{ color: '#64748b', fontSize: '14px' }}>Немає даних для графіка.</p>
            )}
          </div>

          <div className="card">
            <h2>Останні транзакції ({expenses.length})</h2>
            <ul className="expense-list">
              {expenses.map((item) => (
                <li key={item.id} className="expense-item">
                  <div>
                    <strong>{item.description}</strong>
                    <span className="badge">{item.category}</span>
                    <div style={{ fontSize: '12px', color: '#64748b' }}>{item.expenseDate}</div>
                  </div>
                  <strong>{item.amount} {item.currency}</strong>
                </li>
              ))}
            </ul>
          </div>
        </div>

        {/* Права колонка: AI Чат (Tool Calling + RAG) */}
        <div className="card">
          <h2><Bot size={20} color="#2563eb" /> AI Асистент (SQL Tools + RAG)</h2>
          <div className="chat-box">
            {messages.map((m, i) => (
              <div key={i} className={`msg ${m.role}`}>
                {m.text}
              </div>
            ))}
            {isChatLoading && <div className="msg ai">Думаю та перевіряю базу...</div>}
          </div>

          <form onSubmit={handleSendQuestion} className="input-row">
            <input
              type="text"
              placeholder="Скільки я витратив на їжу?"
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              disabled={isChatLoading}
            />
            <button type="submit" disabled={isChatLoading}>
              <Send size={16} />
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

export default App;
