package nightra.reversi.util

class Log(val tag: String) {
  def v(s: String): Unit = android.util.Log.v(tag, s)
  def d(s: String): Unit = android.util.Log.d(tag, s)
  def i(s: String): Unit = android.util.Log.i(tag, s)
  def w(s: String): Unit = android.util.Log.w(tag, s)
  def e(s: String): Unit = android.util.Log.e(tag, s)
}

object Log extends Log("Reversi")