# ShopFlow 前端项目

基于 Vue3 + TypeScript + Element Plus 的电商订单平台前端项目。

## 技术栈

- Vue 3.4
- TypeScript 5.4
- Vite 5.1
- Vue Router 4.3
- Pinia 2.1
- Element Plus 2.6
- Axios 1.6

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 接口
│   │   ├── user.ts       # 用户接口
│   │   ├── product.ts    # 商品接口
│   │   ├── cart.ts       # 购物车接口
│   │   ├── order.ts      # 订单接口
│   │   └── notification.ts # 通知接口
│   ├── components/       # 公共组件
│   │   └── Layout.vue    # 布局组件
│   ├── router/           # 路由
│   │   └── index.ts
│   ├── stores/           # 状态管理
│   │   ├── user.ts       # 用户状态
│   │   └── cart.ts       # 购物车状态
│   ├── styles/           # 样式
│   │   └── index.scss
│   ├── types/            # TypeScript 类型定义
│   │   └── index.ts
│   ├── utils/            # 工具类
│   │   └── request.ts    # axios 封装
│   ├── views/            # 页面
│   │   ├── Home.vue          # 首页
│   │   ├── Login.vue         # 登录页
│   │   ├── Register.vue      # 注册页
│   │   ├── ProductDetail.vue # 商品详情
│   │   ├── Cart.vue          # 购物车
│   │   ├── Order.vue         # 订单
│   │   ├── Notification.vue  # 通知
│   │   └── Profile.vue       # 个人中心
│   ├── App.vue
│   └── main.ts
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
└── README.md
```

## 快速开始

### 安装依赖

```bash
cd frontend
npm install
```

### 开发模式

```bash
npm run dev
```

访问 http://localhost:3000

### 生产构建

```bash
npm run build
```

### 预览构建

```bash
npm run preview
```

## 页面功能

| 页面 | 路径 | 说明 |
|------|------|------|
| 首页 | /home | 商品列表、搜索、分类筛选 |
| 登录 | /login | 用户登录 |
| 注册 | /register | 用户注册 |
| 商品详情 | /product/:id | 商品详情、加入购物车 |
| 购物车 | /cart | 购物车管理 |
| 订单 | /order | 订单列表、订单操作 |
| 通知 | /notification | 消息通知 |
| 个人中心 | /profile | 用户信息、修改密码 |

## API 代理

开发环境下，Vite 代理 `/api` 请求到后端网关：

```typescript
// vite.config.ts
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
```

请确保后端网关服务运行在 8080 端口。
