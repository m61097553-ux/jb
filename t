public static String extractJson(String input) {
    if (input == null) return null;

    int objStart = input.indexOf('{');
    int arrStart = input.indexOf('[');

    if (objStart < 0 && arrStart < 0) return null;

    int start;
    char open;
    char close;

    if (objStart >= 0 && (arrStart < 0 || objStart < arrStart)) {
        start = objStart;
        open = '{';
        close = '}';
    } else {
        start = arrStart;
        open = '[';
        close = ']';
    }

    int balance = 0;
    boolean inString = false;
    boolean escaped = false;

    for (int i = start; i < input.length(); i++) {
        char c = input.charAt(i);

        if (escaped) {
            escaped = false;
            continue;
        }

        if (c == '\\') {
            escaped = true;
            continue;
        }

        if (c == '"') {
            inString = !inString;
            continue;
        }

        if (!inString) {
            if (c == open) balance++;
            else if (c == close) balance--;

            if (balance == 0) {
                return input.substring(start, i + 1);
            }
        }
    }

    return null; // JSON не закрылся
}
