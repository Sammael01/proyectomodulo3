export type Role = 'CLIENT' | 'ADMIN';

export interface AuthResponse {
  token: string;
  userId: number;
  name: string;
  email: string;
  role: Role;
}

export interface UserProfile {
  userId: number;
  name: string;
  email: string;
  role: Role;
}

export interface MessageResponse {
  message: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface VerifyEmailRequest {
  email: string;
  code: string;
}

export interface ResetPasswordRequest {
  email: string;
  code: string;
  newPassword: string;
}

export interface Product {
  id: string;
  name: string;
  category: string;
  price: number;
  oldPrice?: number;
  color: string;
  swatch: string;
  sizes: string[];
  description: string;
  isNew: boolean;
  stock: number;
}

export interface CartLine {
  productId: string;
  size: string;
  qty: number;
}

export interface CartItem extends CartLine {
  product: Product;
}

export interface ShippingInfo {
  fullName: string;
  phone: string;
  address: string;
  city: string;
  department: string;
  notes: string;
}

export type PaymentMethod = 'card' | 'pse' | 'cod';

export interface OrderItem {
  productId: string;
  name: string;
  size: string;
  qty: number;
  price: number;
  swatch: string;
}

export interface Order {
  id: string;
  date: string;
  userEmail: string;
  items: OrderItem[];
  subtotal: number;
  discount: number;
  shipping: number;
  total: number;
  payment: PaymentMethod;
  shippingInfo: ShippingInfo;
  status: 'Confirmado' | 'En preparación' | 'Enviado' | 'Entregado';
}

export interface Video {
  id: string;
  title: string;
  category: string;
  description: string;
  poster: string;
  thumbnail?: string;
  src?: string;
  youtubeId?: string;
  channel?: string;
  views: number;
  likes: number;
  date: string;
  productIds: string[];
}

export interface VideoComment {
  user: string;
  text: string;
  time: string;
}
