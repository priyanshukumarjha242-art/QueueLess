import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/client';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('queueless_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('queueless_token'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchMe = async () => {
      if (token) {
        try {
          const res = await api.get('/auth/me');
          setUser(res.data);
          localStorage.setItem('queueless_user', JSON.stringify(res.data));
        } catch (err) {
          logout();
        }
      }
      setLoading(false);
    };
    fetchMe();
  }, [token]);

  const login = async (email, password) => {
    const res = await api.post('/auth/login', { email, password });
    const data = res.data;
    setToken(data.token);
    setUser(data);
    localStorage.setItem('queueless_token', data.token);
    localStorage.setItem('queueless_user', JSON.stringify(data));
    return data;
  };

  const register = async (registerData) => {
    const res = await api.post('/auth/register', registerData);
    const data = res.data;
    setToken(data.token);
    setUser(data);
    localStorage.setItem('queueless_token', data.token);
    localStorage.setItem('queueless_user', JSON.stringify(data));
    return data;
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('queueless_token');
    localStorage.removeItem('queueless_user');
  };

  const isCustomer = user?.role === 'ROLE_CUSTOMER';
  const isStaff = user?.role === 'ROLE_STAFF';
  const isAdmin = user?.role === 'ROLE_ADMIN';

  return (
    <AuthContext.Provider value={{
      user,
      token,
      loading,
      login,
      register,
      logout,
      isCustomer,
      isStaff,
      isAdmin,
      isAuthenticated: !!token && !!user
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
