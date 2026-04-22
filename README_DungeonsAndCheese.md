# 🧀 DungeonsAndCheese

Um jogo roguelike desenvolvido em Java utilizando LibGDX, focado em progressão por salas, combate e geração procedural.

---

## 🚀 Atualizações Recentes (v0.2)

### ⚙️ Performance
- Uso de **texturas `static`** para evitar múltiplos carregamentos
- Redução de consumo de memória
- Melhoria de FPS geral

---

### 🧠 GameState (Sistema de Estado)
- Controle de:
  - Fase (floor)
  - Score
  - Moedas
- Base preparada para:
  - Upgrades
  - Sistema de progressão

---

### 🗺️ Sistema de Salas (RoomType)

Adicionado sistema de tipos de sala:

| Tipo       | Descrição |
|------------|----------|
| NORMAL     | Combate padrão |
| SAFE       | Sala segura (sem inimigos) |
| TREASURE   | Sala de recompensa |
| BOSS       | Sala de chefe |

✔ Mais variedade  
✔ Melhor pacing de gameplay  

---

### 💰 Sistema de Recompensa
- Salas `TREASURE` agora dão:
  - +Score
  - +Moedas
- Recompensa coletável no centro da sala

---

### 🔫 Combate Melhorado
- Balas:
  - Colidem com paredes
  - São removidas corretamente
- Inimigos:
  - Dão recompensa ao morrer

---

### ❤️ HUD (Interface do Jogador)

Adicionado:
- Barra de vida em caracteres
- Score
- Moedas
- Tipo da sala atual

Exemplo:

Vida: [##########--------] 50/100  
Sala: Tesouro

---

### 🎮 Barra de Vida Animada
- Transição suave ao tomar dano
- Não muda instantaneamente
- Usa interpolação (`displayedHealth`)

---

### 🎨 Cores Dinâmicas
- Verde → vida alta
- Amarelo → média
- Vermelho → crítica

---

### 🧱 Correção de Fonte
Problema:
- Fonte padrão não suporta `█`

Solução:
- Uso de:
  - `#` (cheio)
  - `=` (parcial)
  - `-` (vazio)

---

### 🧹 Gerenciamento de Recursos

Adicionado:
- `dispose()` no `Player`
- `dispose()` seguro no `GameScreen`

Correção importante:
- Removido crash ao morrer (dispose no meio do frame)

---

## 🧠 Arquitetura Atual

Separação clara de responsabilidades:

- `Room` → lógica da sala
- `GameState` → progresso do jogo
- `HUD` → interface
- `GameScreen` → fluxo principal

✔ Código mais organizado  
✔ Fácil de expandir  

---

## 🏁 Próximos Passos

- 🛒 Sistema de loja
- 🔫 Novas armas
- 💥 Efeitos visuais (partículas)
- 🧠 IA mais avançada
- 🎯 Sistema de upgrades

---

## 📌 Versão

**v0.2 — Gameplay & Estrutura**

---

## 👨‍💻 Autor

Projeto desenvolvido por:
**Davi Martins Alexandre**
