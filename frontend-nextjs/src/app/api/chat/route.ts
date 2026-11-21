import { createUIMessageStream, createUIMessageStreamResponse } from 'ai';

export async function POST(req: Request) {
  const { messages } = await req.json();
  const lastMessage = messages[messages.length - 1];

  const question = lastMessage?.parts
    ? lastMessage.parts
        .filter((p: any) => p.type === 'text')
        .map((p: any) => p.text)
        .join('')
    : lastMessage?.content || '';

  const javaBackendUrl = `http://localhost:8080/api/ai/expenses/chat/stream?question=${encodeURIComponent(question)}&chatId=nextjs-session`;
  const javaResponse = await fetch(javaBackendUrl);

  if (!javaResponse.body) {
    return new Response('Java backend stream unavailable', { status: 502 });
  }

  const stream = createUIMessageStream({
    execute: async ({ writer }) => {
      const reader = javaResponse.body!.getReader();
      const decoder = new TextDecoder();
      const textId = crypto.randomUUID();
      let buffer = ''; // Буфер для неповних рядків SSE

      writer.write({ type: 'text-start', id: textId });

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        // Додаємо новий шматок у буфер і ріжемо лише по завершених рядках \n
        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || ''; // Останній незавершений шматок залишаємо в буфері на наступний цикл

        for (const rawLine of lines) {
          const line = rawLine.replace(/\r$/, ''); // Прибираємо \r, якщо Windows-перенос \r\n
          if (line.startsWith('data:')) {
            // Spring WebFlux може віддавати "data:слово" або "data: слово"
            const token = line.startsWith('data: ') ? line.slice(6) : line.slice(5);
            if (token) {
              writer.write({ type: 'text-delta', id: textId, delta: token });
            }
          }
        }
      }

      // Обробляємо залишок у буфері після закриття потоку
      if (buffer.startsWith('data:')) {
        const token = buffer.startsWith('data: ') ? buffer.slice(6) : buffer.slice(5);
        if (token) {
          writer.write({ type: 'text-delta', id: textId, delta: token });
        }
      }

      writer.write({ type: 'text-end', id: textId });
    },
  });

  return createUIMessageStreamResponse({ stream });
}
