class Utils: {
  static equals(a, b) => {
    if (a == null or b == null) return -1;
    else if (a == b) return 1;
    else return 0;
  }

  static isDigit(char) => {
    return char >= 0 and char <= 9;
  }

  static isAlpha(char) => {
    //return (char
  }
}