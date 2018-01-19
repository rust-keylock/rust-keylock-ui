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
import scalafx.scene.image.Image

class Navigation extends ScrollPane {
  fitToHeight = true
  hbarPolicy = ScrollBarPolicy.AsNeeded
  vbarPolicy = ScrollBarPolicy.AsNeeded
  content = new NavigationContent
}
object Navigation {
  private[sides] val imgHeight = 64
  private[sides] val imgWidth = 64
  val Width = imgWidth + 24
}

class NavigationContent extends VBox {
//  spacing = 3

  children = Seq(
    new RklButton {
      tooltip = "Passwords"
      onAction = handle(InterfaceWithRust.INSTANCE.go_to_menu_plus_arg(Defs.MENU_ENTRIES_LIST, Defs.EMPTY_ARG, ""))
      graphic = new ImageView {
        image = new Image("images/circled_list.png")
        fitHeight = Navigation.imgHeight
        fitWidth = Navigation.imgWidth
      }
    },
    new RklButton {
      tooltip = "Encrypt and save"
      onAction = handle(InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_SAVE))
      graphic = new ImageView {
        image = new Image("images/save.png")
        fitHeight = Navigation.imgHeight
        fitWidth = Navigation.imgWidth
      }
    },
    new RklButton {
      tooltip = "Change master password"
      onAction = handle(InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_CHANGE_PASS))
      graphic = new ImageView {
        image = new Image("images/edit.png")
        fitHeight = Navigation.imgHeight
        fitWidth = Navigation.imgWidth
      }
    },
    new RklButton {
      tooltip = "Export to..."
      onAction = handle(InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_EXPORT_ENTRIES))
      graphic = new ImageView {
        image = new Image("images/export.png")
        fitHeight = Navigation.imgHeight
        fitWidth = Navigation.imgWidth
      }
    },
    new RklButton {
      tooltip = "Import from..."
      onAction = handle(InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_IMPORT_ENTRIES))
      graphic = new ImageView {
        image = new Image("images/importimg.png")
        fitHeight = Navigation.imgHeight
        fitWidth = Navigation.imgWidth
      }
    },
    new RklButton {
      tooltip = "Edit Configuration..."
      onAction = handle(InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_SHOW_CONFIGURATION))
      graphic = new ImageView {
        image = new Image("images/settings.png")
        fitHeight = Navigation.imgHeight
        fitWidth = Navigation.imgWidth
      }
    },
    new RklButton {
      tooltip = "Exit"
      onAction = handle(InterfaceWithRust.INSTANCE.go_to_menu(Defs.MENU_EXIT))
      graphic = new ImageView {
        image = new Image("images/close.png")
        fitHeight = Navigation.imgHeight
        fitWidth = Navigation.imgWidth
      }
    })
}