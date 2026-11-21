import React, { createContext, useContext, useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';

const WebSocketContext = createContext();

export const WebSocketProvider = ({ children }) => {
  const [connected, setConnected] = useState(false);
  const clientRef = useRef(null);

  useEffect(() => {
    let stompClient = null;

    try {
      const isHttps = typeof window !== 'undefined' && window.location.protocol === 'https:';
      const wsProtocol = isHttps ? 'wss:' : 'ws:';
      const host = typeof window !== 'undefined' ? window.location.host : 'localhost:5173';
      const brokerURL = `${wsProtocol}//${host}/ws`;

      stompClient = new Client({
        brokerURL: brokerURL,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => {
          setConnected(true);
        },
        onDisconnect: () => {
          setConnected(false);
        },
        onStompError: (frame) => {
          console.warn('STOMP notification:', frame?.headers?.message || frame);
        },
        onWebSocketError: (event) => {
          console.warn('WebSocket status event:', event);
        }
      });

      stompClient.activate();
      clientRef.current = stompClient;
    } catch (err) {
      console.warn('WebSocket init warning:', err);
    }

    return () => {
      if (stompClient) {
        try {
          stompClient.deactivate();
        } catch (e) {
          // ignore
        }
      }
    };
  }, []);

  const subscribe = (destination, callback) => {
    if (!clientRef.current || !clientRef.current.connected) {
      const timer = setTimeout(() => {
        if (clientRef.current && clientRef.current.connected) {
          try {
            clientRef.current.subscribe(destination, (message) => {
              try {
                const body = JSON.parse(message.body);
                callback(body);
              } catch (e) {
                callback(message.body);
              }
            });
          } catch (e) {
            console.warn('Delayed subscription error:', e);
          }
        }
      }, 800);
      return () => clearTimeout(timer);
    }

    try {
      const sub = clientRef.current.subscribe(destination, (message) => {
        try {
          const body = JSON.parse(message.body);
          callback(body);
        } catch (e) {
          callback(message.body);
        }
      });
      return () => {
        try {
          sub.unsubscribe();
        } catch (e) {
          // ignore
        }
      };
    } catch (e) {
      console.warn('Subscription error:', e);
      return () => {};
    }
  };

  return (
    <WebSocketContext.Provider value={{ connected, subscribe }}>
      {children}
    </WebSocketContext.Provider>
  );
};

export const useWebSocket = () => useContext(WebSocketContext);
