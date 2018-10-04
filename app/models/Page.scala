package models

case class Page[T](offset: Int, limit: Int, total: Int, items: List[T]) {
  def isFirst: Boolean = offset == 1
  def isLast: Boolean = limit > total || (offset * limit) == total
}
