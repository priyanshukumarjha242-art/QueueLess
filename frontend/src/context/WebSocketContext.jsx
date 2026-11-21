import React, { createContext, useContext, useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WebSocketContext = createContext();

export const WebSocketProvider = ({ children }) => {
  const [connected, setConnected] = useState(false);
  const clientRef = useRef(null);

  useEffect(() => {
    const socketFactory = () => new SockJS('/ws');
    const stompClient = new Client({
      webSocketFactory: socketFactory,
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
        console.error('STOMP error:', frame);
      }
    });

    stompClient.activate();
    clientRef.current = stompClient;

    return () => {
      if (stompClient) {
        stompClient.deactivate();
      }
    };
  }, []);

  const subscribe = (destination, callback) => {
    if (!clientRef.current || !clientRef.current.connected) {
      // Retry in 500ms if not connected yet
      const timer = setTimeout(() => {
        if (clientRef.current && clientRef.current.connected) {
          clientRef.current.subscribe(destination, (message) => {
            try {
              const body = JSON.parse(message.body);
              callback(body);
            } catch (e) {
              callback(message.body);
            }
          });
        }
      }, 500);
      return () => clearTimeout(timer);
    }

    const sub = clientRef.current.subscribe(destination, (message) => {
      try {
        const body = JSON.parse(message.body);
        callback(body);
      } catch (e) {
        callback(message.body);
      }
    });

    return () => sub.unsubscribe();
  };

  return (
    <WebSocketContext.Provider value={{ connected, subscribe }}>
      {children}
    </WebSocketContext.Provider>
  );
};

export const useWebSocket = () => useContext(WebSocketContext);
