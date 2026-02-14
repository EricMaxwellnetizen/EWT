import { useEffect, useState } from 'react';

interface WebSocketMessage {
  type: 'STORY_UPDATED' | 'STORY_CREATED' | 'PROJECT_UPDATED' | 'NOTIFICATION_CREATED' | 'USER_ACTIVITY';
  entityId: number;
  entityType: string;
  action: string;
  message: string;
  userName: string;
  userId: number;
  timestamp: string;
  payload?: any;
}

export const useWebSocket = (url: string) => {
  const [connected, setConnected] = useState(false);
  const [messages, setMessages] = useState<WebSocketMessage[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // WebSocket URL - adjust protocol based on current location
    const wsUrl = url.replace('http://', 'ws://').replace('https://', 'wss://');
    
    let stompClient: any;
    let ws: any;

    const connect = () => {
      try {
        // Using SockJS with STOMP protocol
        ws = new (window as any).SockJS(wsUrl);
        stompClient = (window as any).Stomp.over(ws);

        stompClient.connect({}, (frame: any) => {
          console.log('WebSocket connected:', frame);
          setConnected(true);

          // Subscribe to global updates
          stompClient.subscribe('/topic/updates', (message: any) => {
            try {
              const parsedMessage = JSON.parse(message.body) as WebSocketMessage;
              setMessages((prev) => [parsedMessage, ...prev.slice(0, 49)]);
            } catch (e) {
              console.error('Error parsing WebSocket message:', e);
            }
          });

          // Subscribe to personal notifications
          stompClient.subscribe('/user/queue/notifications', (message: any) => {
            try {
              const parsedMessage = JSON.parse(message.body) as WebSocketMessage;
              setMessages((prev) => [parsedMessage, ...prev.slice(0, 49)]);
            } catch (e) {
              console.error('Error parsing personal notification:', e);
            }
          });
        }, (error: any) => {
          console.error('WebSocket connection error:', error);
          setError(error);
          setTimeout(() => connect(), 5000); // Reconnect after 5 seconds
        });
      } catch (err) {
        console.error('WebSocket setup error:', err);
        setError(String(err));
      }
    };

    connect();

    return () => {
      if (stompClient && stompClient.connected) {
        stompClient.disconnect(() => {
          console.log('WebSocket disconnected');
          setConnected(false);
        });
      }
    };
  }, [url]);

  return { connected, messages, error };
};

export default useWebSocket;
