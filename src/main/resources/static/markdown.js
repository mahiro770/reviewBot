/**
 * Geminiが返すMarkdown形式のテキスト(問題文・レビュー結果)を、
 * 最小限のサブセットだけHTMLに変換する軽量パーサー。
 * ビルドツールを使わない構成のため、外部ライブラリは使わない。
 * 対応: 見出し(##/###/####)、太字(**text**)、インラインコード(`code`)、
 * フェンス付きコードブロック(```)、箇条書き(-/*)、番号付きリスト(1.)、段落。
 * Geminiの出力は信頼できない入力として扱い、HTMLは必ずエスケープしてから組み立てる。
 */

function escapeHtml(str) {
    return str
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;");
}

function renderMarkdownInline(escapedText) {
    let html = escapedText.replace(/`([^`]+)`/g, (_, code) => `<code>${code}</code>`);
    html = html.replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>");
    return html;
}

function renderMarkdown(raw) {
    if (!raw) return "";

    // フェンス付きコードブロックを先に抜き出し、プレースホルダに置き換える
    // (中身の**や`をインライン変換の対象にしないため。中身はここで個別にエスケープする)
    const codeBlocks = [];
    let text = raw.replace(/```[ \t]*\w*\n?([\s\S]*?)```/g, (_, code) => {
        const index = codeBlocks.length;
        codeBlocks.push(escapeHtml(code.replace(/\n$/, "")));
        return "\nCODEBLOCK_PLACEHOLDER_" + index + "\n";
    });

    text = escapeHtml(text);

    const blocks = [];
    let paragraph = [];
    let list = null;

    function flushParagraph() {
        if (paragraph.length) {
            blocks.push("<p>" + renderMarkdownInline(paragraph.join(" ")) + "</p>");
            paragraph = [];
        }
    }
    function flushList() {
        if (list) {
            const items = list.items.map(item => "<li>" + renderMarkdownInline(item) + "</li>").join("");
            blocks.push("<" + list.type + ">" + items + "</" + list.type + ">");
            list = null;
        }
    }

    const lines = text.split("\n");
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i].trim();

        if (line.indexOf("CODEBLOCK_PLACEHOLDER_") === 0) {
            const idx = Number(line.slice("CODEBLOCK_PLACEHOLDER_".length));
            flushParagraph();
            flushList();
            blocks.push("<pre><code>" + codeBlocks[idx] + "</code></pre>");
            continue;
        }

        if (line === "") {
            flushParagraph();
            flushList();
            continue;
        }

        const headingMatch = line.match(/^(#{1,4})\s+(.*)$/);
        if (headingMatch) {
            flushParagraph();
            flushList();
            // review-textではh3/h4の2段階しかスタイルを用意していないため、
            // #の数(1〜4)はその2段階にまとめる(#1つでも巨大なh1にはしない)
            const level = headingMatch[1].length <= 2 ? 3 : 4;
            blocks.push("<h" + level + ">" + renderMarkdownInline(headingMatch[2]) + "</h" + level + ">");
            continue;
        }

        const bulletMatch = line.match(/^[-*]\s+(.*)$/);
        if (bulletMatch) {
            flushParagraph();
            if (!list || list.type !== "ul") {
                flushList();
                list = { type: "ul", items: [] };
            }
            list.items.push(bulletMatch[1]);
            continue;
        }

        const numberedMatch = line.match(/^\d+\.\s+(.*)$/);
        if (numberedMatch) {
            flushParagraph();
            if (!list || list.type !== "ol") {
                flushList();
                list = { type: "ol", items: [] };
            }
            list.items.push(numberedMatch[1]);
            continue;
        }

        flushList();
        paragraph.push(line);
    }
    flushParagraph();
    flushList();

    return blocks.join("");
}
