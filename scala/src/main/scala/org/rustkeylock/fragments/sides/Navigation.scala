package org.rustkeylock.fragments.sides

import scalafx.scene.layout.VBox
import org.rustkeylock.components.RklButton
import scalafx.Includes._
import scalafx.scene.image.ImageView
import scalafx.scene.control.ScrollPane
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.control.Tooltip.stringToTooltip
import org.rustkeylock.utils.Defs
import org.rustkeylock.api.InterfaceWithRust

class Navigation extends ScrollPane {
  fitToHeight = true
  hbarPolicy = ScrollBarPolicy.AsNeeded
  vbarPolicy = ScrollBarPolicy.AsNeeded
  content = new NavigationContent
}

class NavigationContent extends VBox {
  spacing = 3

  children = Seq(
    new RklButton {
      tooltip = "Passwords"
      onAction = handle(InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_ENTRIES_LIST))
      graphic = new ImageView("images/circled_list.png")
    },
    new RklButton {
      tooltip = "Encrypt and save"
      onAction = handle(InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_SAVE))
      graphic = new ImageView("images/save.png")
    },
    new RklButton {
      tooltip = "Change master password"
      onAction = handle(InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_CHANGE_PASS))
      graphic = new ImageView("images/edit.png")
    },
    new RklButton {
      tooltip = "Export to..."
      onAction = handle(InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_EXPORT_ENTRIES))
      graphic = new ImageView("images/export.png")
    },
    new RklButton {
      tooltip = "Import from..."
      onAction = handle(InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_IMPORT_ENTRIES))
      graphic = new ImageView("images/importimg.png")
    },
    new RklButton {
      tooltip = "Exit"
      onAction = handle(InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_EXIT))
      graphic = new ImageView("images/close.png")
    })
}