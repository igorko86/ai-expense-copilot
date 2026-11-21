'use client'; // Вмикаємо клієнтський React (потрібно для хуків useState та useChat)

import { useState } from 'react';
import { useChat } from '@ai-sdk/react';
import { Send, Bot, Wallet } from 'lucide-react';

export default function Home() {
  const [input, setInput] = useState('');

  // Хук Vercel AI SDK: за замовчуванням сам відправляє запити на /api/chat
  const { messages, sendMessage, status } = useChat();
  const isLoading = status === 'submitted' || status === 'streaming';

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!input.trim() || isLoading) return;
    sendMessage({ text: input }); // Відправляємо нове повідомлення
    setInput('');
  }

  return (
    <main className="min-h-screen bg-slate-100 p-6 flex justify-center">
      <div className="w-full max-w-2xl bg-white rounded-xl shadow-md p-6 flex flex-col h-[80vh]">
        {/* Шапка */}
        <div className="flex items-center gap-3 pb-4 border-b border-slate-200">
          <Wallet className="text-blue-600" size={28} />
          <div>
            <h1 className="text-xl font-bold text-slate-800">AI Expense Copilot</h1>
            <p className="text-xs text-slate-500">Next.js App Router + Vercel AI SDK + Java Spring AI</p>
          </div>
        </div>

        {/* Вікно чату */}
        <div className="flex-1 overflow-y-auto py-4 space-y-4">
          {messages.length === 0 && (
            <div className="text-center text-slate-400 mt-10 text-sm">
              Запитай мене про твої витрати з бази PostgreSQL (наприклад: «Скільки я витратив на їжу?»)
            </div>
          )}

          {messages.map((m) => (
            <div
              key={m.id}
              className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}
            >
              <div
                className={`max-w-[80%] rounded-xl px-4 py-3 text-sm whitespace-pre-wrap ${
                  m.role === 'user'
                    ? 'bg-blue-600 text-white'
                    : 'bg-slate-100 text-slate-800 border border-slate-200'
                }`}
              >
                {/* У Vercel AI SDK контент повідомлення зберігається у масиві parts */}
                {m.parts.map((part, idx) =>
                  part.type === 'text' ? <span key={idx}>{part.text}</span> : null
                )}
              </div>
            </div>
          ))}

          {isLoading && (
            <div className="flex items-center gap-2 text-xs text-slate-400">
              <Bot size={16} className="animate-bounce" />
              Java Spring AI аналізує дані...
            </div>
          )}
        </div>

        {/* Форма відправки */}
        <form onSubmit={handleSubmit} className="pt-4 border-t border-slate-200 flex gap-2">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Напиши запитання..."
            disabled={isLoading}
            className="flex-1 px-4 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:border-blue-500 text-slate-800"
          />
          <button
            type="submit"
            disabled={isLoading}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg font-medium flex items-center gap-2 hover:bg-blue-700 disabled:bg-slate-400 cursor-pointer"
          >
            <Send size={16} />
          </button>
        </form>
      </div>
    </main>
  );
}
