import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Main from './pages/Main';
import ProductList from './pages/ProductList';
import ProductDetail from './pages/ProductDetail';
import Cart from './pages/Cart';

function App() {
  return (
      <BrowserRouter>
          <Routes>
              <Route path="/" element={<Main />} />
              <Route path="/login" element={<Login />} />
              <Route path="/signup" element={<Signup />} />
              <Route path="/cart" element={<Cart />} />
              {/* 대분류 클릭 시 (예: /product/keyboard, /product/switch, /product/keycap) */}
              <Route path="/product/:category" element={<ProductList />} />

              {/* 소분류 클릭 시 (예: /product/keyboard/mechanical, /product/switch/linear 등) */}
              <Route path="/product/:category/:subCategory" element={<ProductList />} />

              <Route path="/product/detail/:id" element={<ProductDetail />} />
          </Routes>
      </BrowserRouter>
  );
}

export default App;