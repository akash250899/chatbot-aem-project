document.addEventListener('DOMContentLoaded', () => {
    // Note: We now select the wrapper instead of just the chat box
    const chatWidgets = document.querySelectorAll('.cmp-rag-chat-wrapper');

    chatWidgets.forEach(widget => {
        const chatContainer = widget.querySelector('.cmp-rag-chat');
        const launcher = widget.querySelector('.cmp-rag-chat__launcher');
        const closeBtn = widget.querySelector('.cmp-rag-chat__close');
        
        const input = widget.querySelector('.cmp-rag-chat__input');
        const sendBtn = widget.querySelector('.cmp-rag-chat__send-btn');
        const messagesContainer = widget.querySelector('.cmp-rag-chat__messages');
        const windowContainer = widget.querySelector('.cmp-rag-chat__window');
        
        const namespace = widget.dataset.namespace;
        const apiUrl = widget.dataset.apiUrl;
        const sessionId = "sess-" + Math.random().toString(36).substring(2, 10);
        let isWaiting = false;

        // Toggle Widget Open/Close
        launcher.addEventListener('click', () => {
            widget.classList.add('is-open');
            input.focus();
        });

        closeBtn.addEventListener('click', () => {
            widget.classList.remove('is-open');
        });

        const scrollToBottom = () => {
            windowContainer.scrollTop = windowContainer.scrollHeight;
        };

        const addMessage = (text, type = 'system', isHtml = false) => {
            const wrapper = document.createElement('div');
            wrapper.className = `cmp-rag-chat__message-wrapper cmp-rag-chat__message-wrapper--${type}`;
            
            const msg = document.createElement('div');
            msg.className = `cmp-rag-chat__message ${type === 'error' ? 'cmp-rag-chat__message--error' : ''}`;
            
            if (isHtml) {
                msg.innerHTML = text; // Required to render the source links
            } else {
                msg.textContent = text;
            }

            wrapper.appendChild(msg);
            messagesContainer.appendChild(wrapper);
            scrollToBottom();
            return wrapper; 
        };

        const showTypingIndicator = () => {
            const typingHtml = `
                <div class="cmp-rag-chat__typing">
                    <div class="cmp-rag-chat__dot"></div>
                    <div class="cmp-rag-chat__dot"></div>
                    <div class="cmp-rag-chat__dot"></div>
                </div>`;
            return addMessage(typingHtml, 'system', true);
        };

        const handleSend = async () => {
            const question = input.value.trim();
            if (!question || isWaiting) return;

            isWaiting = true;
            sendBtn.disabled = true;
            input.value = '';
            addMessage(question, 'user');
            const typingIndicator = showTypingIndicator();

            try {
                const response = await fetch(apiUrl, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify({
                        question: question,
                        namespace: namespace,
                        sessionId: sessionId
                    })
                });

                typingIndicator.remove(); 

                if (!response.ok) throw new Error(`HTTP ${response.status}`);

                const data = await response.json();
                
                // 1. Get the main text answer
                let finalHtml = data.answer || "I found no direct answer.";

                // 2. Format the sources if they exist in the JSON
                if (data.sources && Array.isArray(data.sources) && data.sources.length > 0) {
                    let sourcesHtml = `<div class="cmp-rag-chat__sources">
                                          <div class="cmp-rag-chat__sources-title">Reference Sources:</div>`;
                    
                    data.sources.forEach((src, index) => {
                        // Creating styled link pills for each source
                        sourcesHtml += `
                            <a href="${src.url}" target="_blank" class="cmp-rag-chat__source-link" title="${src.title}">
                                [${index + 1}] ${src.title}
                            </a>`;
                    });
                    
                    sourcesHtml += `</div>`;
                    
                    // Append sources HTML to the main answer
                    finalHtml += sourcesHtml;
                }

                // Ensure we pass `true` as the third argument so innerHTML processes the links
                addMessage(finalHtml, 'system', true);

            } catch (error) {
                if(typingIndicator) typingIndicator.remove();
                console.error("RAG API Error:", error);
                addMessage("Sorry, I'm having trouble connecting to the server.", 'error');
            } finally {
                isWaiting = false;
                sendBtn.disabled = false;
                input.focus();
            }
        };

        sendBtn.addEventListener('click', handleSend);
        input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') handleSend();
        });
    });
});