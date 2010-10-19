package au.ken.treeview

import au.ken.treeview.event._
import scala.swing._
import javax.swing.{CellEditor => JCellEditor, AbstractCellEditor => JAbstractCellEditor}
import javax.swing.event.{CellEditorListener, ChangeEvent}


trait EditableCellsCompanion {
  type Editor[A] <: CellEditor[A]
  protected type Owner <: Component with CellView[_]
  
  val Editor: CellEditorCompanion
  

  trait CellEditorCompanion {
    type Peer <: JCellEditor
    type Params
    val emptyParams: Params
    def wrap[A](e: Peer): Editor[A]
    def apply[A, B: Editor](toB: A => B, toA: B => A): Editor[A]
  }
  
  trait CellEditor[A] extends Publisher with au.ken.treeview.CellEditor[A] {
    val companion: CellEditorCompanion
    def peer: companion.Peer

    protected def fireCellEditingCancelled() {publish(CellEditingCancelled(CellEditor.this))}
    protected def fireCellEditingStopped() {publish(CellEditingStopped(CellEditor.this))}

    protected def listenToPeer(p: JCellEditor) {
      p.addCellEditorListener(new CellEditorListener {
        override def editingCanceled(e: ChangeEvent) {fireCellEditingCancelled()}
        override def editingStopped(e: ChangeEvent) {fireCellEditingStopped()}
      })
    }

    abstract class EditorPeer extends JAbstractCellEditor {
      override def getCellEditorValue(): AnyRef = value.asInstanceOf[AnyRef]
      listenToPeer(this)
    }

    def componentFor(owner: Owner, value: A, params: companion.Params = companion.emptyParams): Component
    
    def cellEditable = peer.isCellEditable(null)
    def shouldSelectCell = peer.shouldSelectCell(null)
    def cancelCellEditing() = peer.cancelCellEditing
    def stopCellEditing() = peer.stopCellEditing
  }
}