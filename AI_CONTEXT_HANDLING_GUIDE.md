# AI Context Handling in Phase 6 🧠

## How AI Context Works

### 📊 **Context Architecture**

```
User Message → Short-term Memory → AI Processing → Context-aware Response
     ↓                    ↓                           ↑
Database Storage    Token Management            Context Retrieval
     ↓                    ↓                           ↑
Long-term Synthesis  Memory Optimization     Relevant Memory Search
```

### 🔄 **Context Persistence**

**✅ Survives App Restarts**: Yes! Context is stored in the database, not in-memory
**✅ Cross-Session Memory**: Conversations continue where you left off
**✅ Smart Context Management**: Automatic cleanup prevents context bloat

### 📝 **Context Flow Example**

```bash
# First conversation
> ask "What's the best way to save battery?"
AI: "To save battery, you can..."

# Close app, reopen later
> ask "Can you remind me what you said about battery?"
AI: "I previously mentioned that to save battery, you can..."
   # ↑ AI retrieves context from database automatically
```

## 🎯 **Context Size Management**

### **Smart Limits**
- **Short-term Memory**: 50 messages max per conversation
- **Token Limit**: 8,000 tokens per conversation
- **Memory Decay**: 30 days for unused long-term memories
- **Automatic Cleanup**: Promotes important messages to long-term storage

### **Context Size Control**

```kotlin
// Constants from AIMemoryManager.kt
MAX_SHORT_TERM_MESSAGES = 50        // Recent conversation limit
MAX_TOKENS_PER_CONVERSATION = 8000  // Context size limit
REFLECTION_TRIGGER_THRESHOLD = 10   // Generate insights every 10 messages
MIN_IMPORTANCE_SCORE = 0.3f         // Only keep important memories (30%+)
```

### **How Context Stays Manageable**

1. **Importance Scoring**: Each message gets a score (0.0-1.0)
   - Questions get higher scores
   - Commands get boosted importance
   - AI responses are valuable (processed content)
   - Technical keywords increase importance

2. **Memory Promotion**: When limits are reached:
   - AI synthesizes old messages into concise summaries
   - Only messages with importance ≥ 0.3 are preserved
   - Summaries go to long-term memory (compressed)
   - Original messages are cleaned up

3. **Intelligent Retrieval**: When you ask something:
   - AI searches relevant memories (keyword matching)
   - Recent conversation context (last 20 messages)
   - Long-term memories related to your query
   - Only relevant context is included in prompt

## 💡 **Context Usage Examples**

### **Conversation Continuity**
```bash
# Session 1
> ask "How can I improve app performance?"
AI: "You can improve app performance by..."

# Session 2 (after app restart)
> ask "What about the memory optimization you mentioned?"
AI: "I mentioned memory optimization for app performance..."
   # ↑ Retrieves context from previous session
```

### **Memory Search**
```bash
> memory search --query="battery"
Found 3 memories:
1. User asked about battery optimization techniques
2. Discussion about power-saving WiFi settings  
3. Recommendation to use 'power battery' command
```

### **Context Management**
```bash
> memory stats
Long-term memories: 15
Reflections: 3
Average importance: 67%

> memory cleanup
✅ Memory cleanup completed!
• Old and low-importance memories cleaned up
```

## 🚀 **Advanced Context Features**

### **Multiple Conversation Threads**
```bash
# Different conversation contexts
> ask --context=battery_help "Tell me about battery saving"
> ask --context=performance_tuning "How to optimize memory?"

# Each context maintains separate history
```

### **Memory Reflection System**
- AI generates insights every 10 messages
- Identifies usage patterns and preferences
- Creates actionable recommendations
- Learns from conversation history

### **Context-Aware Commands**
```bash
> interpret "make my battery last longer"
Intent: Battery optimization
Confidence: 89%
Suggested Commands:
1. power battery
2. optimize --category=battery
```

## 🎛️ **Context Control Commands**

### **Memory Management**
```bash
memory show           # View current memory status
memory search --query="topic"  # Search conversation history
memory stats          # Detailed memory statistics
memory cleanup        # Clean old/unimportant memories
memory reflect        # Generate new insights
```

### **Conversation Control**
```bash
ask "question"                    # Use default context
ask --memory "question"           # Include relevant memories (default)
ask --context=conv123 "question"  # Use specific conversation
ask --memory=false "question"     # No memory context (fresh start)
```

## 🔧 **Technical Implementation**

### **Database Storage**
- **short_term_memory**: Recent messages with metadata
- **long_term_memory**: Synthesized important conversations
- **reflections**: AI-generated insights and patterns

### **Context Building Process**
1. **Recent Context**: Last 10-20 messages from conversation
2. **Relevant Memories**: Keyword search in long-term storage
3. **Synthesis**: AI combines context into coherent prompt
4. **Response Generation**: Context-aware AI response
5. **Memory Storage**: Save new message and response

### **Performance Optimization**
- **Lazy Loading**: Context retrieved only when needed
- **Compression**: Long conversations synthesized into summaries
- **Indexing**: Fast keyword searches in memory database
- **Cleanup**: Automatic removal of outdated memories

## 📈 **Context Benefits**

### **For Users**
- ✅ Conversations feel natural and continuous
- ✅ AI remembers your preferences and past discussions
- ✅ No need to repeat context in every message
- ✅ Smart suggestions based on usage patterns

### **For Performance**
- ✅ Context size stays manageable (no exponential growth)
- ✅ Relevant information is prioritized
- ✅ Database storage is efficient with cleanup
- ✅ Fast context retrieval with indexing

## 🔮 **Context Future Enhancements**

- **Semantic Search**: Vector embeddings for better memory retrieval
- **Cross-User Learning**: Anonymized pattern sharing
- **Adaptive Context**: Dynamic context size based on conversation complexity
- **Context Export**: Backup and restore conversation history

---

**The AI context system ensures that conversations feel natural and continuous while maintaining excellent performance through intelligent memory management.**