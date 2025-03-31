# Board game

This repository contains a basic graphical user interface for games with mechanics analogous to the ones of
[Might & Magic: Clash of Heroes](https://www.dotemu.com/games/might-magic-clash-of-heroes-definitive-edition/) or
[Legend of Solgard](https://snowprintstudios.com/solgard/).

The graphical interface uses the [libGDX](https://libgdx.com/) library.\
All images were downloaded from [Game-icons.net](https://game-icons.net/).

## Requirements

- Java JDK version 17 or higher
- Gradle version 7.2 or higher

## Usage

### From a terminal

First, run the Gradle wrapper (once is enough):

```bash
gradle wrapper
```
#### Starting the application

- on macOS/Linux:

```bash
./gradlew run
```

- on Windows:

```bash
gradlew.bat run
```

#### Running the unit tests

- on macOS/Linux:

```bash
./gradlew test
```

- on Windows:

```bash
gradlew.bat test
```


### With an IDE
Open this repository as a Gradle project.

Then to start the application, run the method [DesktopLauncher.main](desktop/src/it/unibz/inf/pp/clash/DesktopLauncher.java)
  (in your running configuration, you may need to specify `assets` as the Java working directory).


## Design

### Game snapshot

The project is designed around the notion of game _snapshot_.\
A snapshot is an object that intuitively stores all the information needed to resume an interrupted game (state of the board, remaining health, active player, etc.).\
In other words, you can think of a snapshot as a save state.

- The [Snapshot](core/src/main/java/it/unibz/inf/pp/clash/model/snapshot/Snapshot.java) Java interface specifies which information a snapshot should contain.\
  The documentation of this interface
  (and the interfaces that it refers to, transitively) should be sufficient for you to understand what a snapshot is.\
  In particular:

  - The method [Snapshot.getBoard](core/src/main/java/it/unibz/inf/pp/clash/model/snapshot/Snapshot.java)
    should return the current board.

  - A board must in turn implement the interface [Board](core/src/main/java/it/unibz/inf/pp/clash/model/snapshot/Board.java).\
    A board is a rectangular grid.
    Rows and columns on the board are indexed from left to right and top to bottom, with natural numbers, starting at index 0.
    So the upper-left tile has index (0,0)

    _Note_: several (adjacent) tiles may be occupied by the same unit (i.e. by the same Java object).

  - A unit must implement the interface [Unit](core/src/main/java/it/unibz/inf/pp/clash/model/snapshot/units/Unit.java).\
    You will find several implementations of this interface ([Unicorn](core/src/main/java/it/unibz/inf/pp/clash/model/snapshot/units/impl/Unicorn.java), etc.).
    Feel free to modify them and/or add your own.

- The project only contains a [dummy implementation](core/src/main/java/it/unibz/inf/pp/clash/model/impl/dummy/snapshot/DummySnapshot.java) of the Snapshot interface.\
  _Tip:_ in order to implement it properly, you can create a class that extends the abstract class [AbstractSnapshot](core/src/main/java/it/unibz/inf/pp/clash/model/snapshot/impl/AbstractSnapshot.java).

### Model-view-controller

In order to decouple the graphical interface from the mechanics of the game, the project (loosely) follows the [model-view-controller](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller) (MVC) pattern.\
This means that the code is partitioned into three components called
[model](core/src/main/java/it/unibz/inf/pp/clash/model/README.md),
[view](core/src/main/java/it/unibz/inf/pp/clash/view/README.md) and
[controller](core/src/main/java/it/unibz/inf/pp/clash/controller/README.md):

- The _controller_ registers the user actions (click, hovering, etc.), and notifies the model of each action.
- The _model_ is the core of the application.
  It keeps track of the state of the game and updates it after each action.
  The model takes its input from the controller, and outputs drawing instructions to the view.
- The _view_ is in charge of drawing the game on screen. It takes its input from the model.

_Note:_ This is not the most common interpretation of the MVC pattern: in many applications, the model remains passive,
whereas in this project the model gives instructions to the view.

## How to implement your own game

The controller and the view components (a.k.a. the graphical interface) are already implemented.
So In order to develop your own game, you only need to implement the model (i.e. the mechanics of the game).\
To this end, you can modify and extend the content of the folder [model](core/src/main/java/it/unibz/inf/pp/clash/model/README.md).

Your code only needs to respect the interfaces that specify:

- how the controller communicates with the model, and
- how the model communicates with the view.

Here is a brief description of these two interfaces:

#### The controller-model interface

- The [EventHandler](core/src/main/java/it/unibz/inf/pp/clash/model/EventHandler.java) Java interface specifies how the controller
  notifies the model of user actions.\
  For instance, when the user clicks on the "exit game" button, the controller calls the method
  [EventHandler.exitGame](core/src/main/java/it/unibz/inf/pp/clash/model/EventHandler.java).

- Your primary task is to implement the methods of the [EventHandler](core/src/main/java/it/unibz/inf/pp/clash/model/EventHandler.java) interface.\
  For now, the code only contains a [dummy implementation](core/src/main/java/it/unibz/inf/pp/clash/model/impl/DummyEventHandler.java).
  You should instead create your own class that implements this interface.\
  After creating this class, you can incorporate it to the project as follows:
  in the method [DesktopLauncher.main](desktop/src/it/unibz/inf/pp/clash/DesktopLauncher.java), replace the instruction

```Java
        EventHandler eventHandler = new DummyEventHandler(displayManager);
```

with

```Java
        EventHandler eventHandler = new MyEventHandler(displayManager);
```

where `MyEventHandler` is the name of your class.

#### The model-view interface

- The [DisplayManager](core/src/main/java/it/unibz/inf/pp/clash/view/DisplayManager.java) Java interface specifies how the model
  provides drawing instructions to the view.
  This interface is already implemented (with the class [DisplayManagerImpl](core/src/main/java/it/unibz/inf/pp/clash/view/impl/DisplayManagerImpl.java)), as part of the view component.
  You can implement a fully functional game without modifying this implementation.

- In particular, you can draw a snapshot on screen by calling the method [DisplayManager.drawSnapshot](core/src/main/java/it/unibz/inf/pp/clash/view/DisplayManager.java)
  (with your snapshot as argument).\
  If there was another snapshot on screen prior to this call, then the two snapshots will be compared and their differences highlighted,
  with a fade-in animation.\
  Use these animations to notify the user of the effects of his actions.\
  Note that sending snapshots during an animation will not interrupt it.
  Instead, your snapshots will be buffered, and displayed after all pending animations have terminated.

## To go further

You can implement a fully functional game (player VS player and/or player VS bot) by modifying the model component only,
without altering the code in the
[view](core/src/main/java/it/unibz/inf/pp/clash/view/README.md) or
[controller](core/src/main/java/it/unibz/inf/pp/clash/controller/README.md) folders.

However, you may also want to modify the graphical interface:

- For (lightweight) aesthetic customization (e.g. changing colors),
  you can edit some of the property files of the project.
  They are all located in the `assets` folder, and their precise locations are listed in the [FileManager](core/src/main/java/it/unibz/inf/pp/clash/view/singletons/FileManager.java) class.\
  You can also add your own (.png) images under `assets/images/png`:
  place the images in the appropriate subfolders (the folder structure should be self-explanatory),
  and reference them in the appropriate property files (e.g. in [this file](assets/images/portraits.properties) for the portrait of a hero).
- For more advanced customization, you can follow [online tutorials](https://libgdx.com/wiki/start/demos-and-tutorials) about LibGDX.
