# Effects

## What is an Effect?

There are cases when some piece of game logic is triggered by multiple different
events. Consider Quake 3 Arena. There are several ways to select a weapon:
- When you pick up a new weapon, by default it is selected immediately, unless
you already had this weapon;
- Another way to select a weapon is to press a corresponding key, like 4 for the
Lightning Gun in Quake 3;
- If you were shooting furiously from the Rocket Launcher and ran out of ammo,
then another weapon is selected automatically.

If Q3 was built following the ECS pattern and if Systems were small, then these
three preconditions of weapon selection would probably be handled by different
Systems. And it feels natural to decouple the part which switches events and
extract it to another System. Normally Systems operate on Components, so first
thing that comes to mind is to create a Component like "IntentWeaponChange" and
put data there, then process it in a System. But this component does not really
represent state, it's just for an event. Feels like a workaround. And instead of
a workaround of this kind, Cotta provides mechanism of Effects.

Effect is a notification about something that happened. Effect is processed in
the same tick it was created, provided the System handling it is invoked after
the System that fired it.

## Example

Let's modify the example from the [Quick start](quick-start.md) to demonstrate
possible usage of Effects. We will add shooting to that game. We need:
- Input button for shooting
- Cooldown
- Bullet entity

### Input

First we one more parameter: `isShooting`

```kotlin
data class ShowcasePlayerInput(
    val walkingDirection: Byte,
    val isShooting: Boolean
) : PlayerInput
```

And handle button press:

```kotlin
class ShowcaseCottaClientGdxInput : CottaClientGdxInput {
    private val storage = Storage()

    override fun accumulate() {
        with(storage) {
            // ...
            shootPressed = shootPressed || Gdx.input.isKeyPressed(Input.Keys.SPACE)
        }
    }

    override fun input(): ShowcasePlayerInput {
        return ShowcasePlayerInput(
            walkingDirection = /* ... */
            isShooting = storage.shootPressed
        ).also { clear() }
    }

    private fun clear() {
        // ...
        storage.shootPressed = false
    }

    private class Storage {
        // ...
        var shootPressed: Boolean = false
    }
}
```

Since we use `ControllableComponent` there as a Component which Systems use, we
also add `isShooting` to it:

```kotlin
interface ControllableComponent : MutableComponent<ControllableComponent> {
    var direction: Byte
    var isShooting: Boolean
    val playerId: PlayerId
}
```

And we need to modify input processing to pass this new `Boolean` from input to
the component:

```kotlin
class ShowcaseInputProcessing : InputProcessing {
    override fun processPlayerInput(
        playerId: PlayerId,
        input: PlayerInput,
        entities: Entities,
        effectBus: EffectBus
    ) {
        // ...
        controllable.isShooting = input.isShooting
    }

    // ...
}
```

### Adjustments to components attached to the player's square entity

_This is not exactly necessary to demonstrate Effects, but it makes sense from
the gameplay perspective, and it doesn't hurt to once again touch components to
get more used to them._

Since we are making our little square shoot, we need to know where to shoot. We
will introduce orientation to the square, so that when the shoot button is
pressed, it shoots at the direction the square is facing.

```kotlin
interface PositionComponent : MutableComponent<PositionComponent> {
    @Interpolated var x: Float
    @Interpolated var y: Float
    var orientation: Byte
}

object Orientation {
    const val UP: Byte = 0
    const val RIGHT: Byte = 1
    const val DOWN: Byte = 2
    const val LEFT: Byte = 3
}
```

Now try `gradle clean build` and see compilation errors in the
`ShowcasePlayersHandler` class. Since we have modified `PositionComponent` and
`ControllableComponent`, their creation functions were regenerated, and now they
require new parameters:

```kotlin
override fun onEnterGame(playerId: PlayerId, entities: Entities) {
    entities.create(ownedBy = Entity.OwnedBy.Player(playerId)).apply {
        addComponent(createControllableComponent(
            direction = WalkingDirections.IDLE,
            isShooting = false,
            playerId = playerId
        ))
        addComponent(createPositionComponent(
            x = 120f,
            y = 120f,
            orientation = Orientation.RIGHT
        ))
    }
}
```

### Firing an effect

Let's define the effect that we will use to shoot. Akin to Components, Effects
are defined as interfaces. They must implement `CottaEffect` and be immutable.
And, just like Components, they have to be defined in the same or nested
package as the class implementing `CottaGame` annotated with `@Game`.

```kotlin
interface ShootEffect : CottaEffect {
    val shooterId: EntityId
}
```

The idea is to fire this effect from one system and handle it in another. That
another system will retrieve the shooter's position and orientation and create a
bullet accordingly.

So, the system that fires the effect (again, execute `gradle clean build` to
generate the `createShootEffect` function):

```kotlin
class ShootingSystem : EntityProcessingSystem {
    override fun process(e: Entity, ctx: EntityProcessingContext) {
        if (e.hasComponent(ControllableComponent::class) &&
            e.hasComponent(PositionComponent::class)
        ) {
            val controllable = e.getComponent(ControllableComponent::class)
            if (controllable.isShooting) {
                ctx.fire(createShootEffect(e.id))
            }
        }
    }
}
```

Note that we check the existence of `PositionComponent` even though we don't use
it here. That's because we can't shoot without knowing the shooter's position.

And now the system that handles the effect:

```kotlin
class ShootEffectConsumer : EffectsConsumerSystem<ShootEffect> {
    override val effectType: Class<ShootEffect> = ShootEffect::class.java

    override fun handle(e: ShootEffect, ctx: EffectProcessingContext) {
        val shooter = ctx.entities().get(e.shooterId) ?: return
        val position = shooter.getComponent(PositionComponent::class)
        val bullet = ctx.createEntity()
        bullet.addComponent(createPositionComponent(position.x, position.y, position.orientation))
    }
}
```

And we need to mention them in the class implementing `CottaGame`:

```kotlin
@Game
class ShowcaseGame : CottaGame {
    // ...
    override val systems: List<CottaSystem> = listOf(
        MovementSystem(),
        ShootingSystem(),
        ShootEffectConsumer()
    )
}
```

Let's take a moment to `gradle clean build`, then `gradle server:run` and
`gradle lwjgl3:run` to see what we have. Now after we press the space button,
new entity is created in the same position. Since we draw all entities as
squares regardless of their components, "bullets" are also just squares. And
they don't move, because we haven't implemented their movement yet. You can find
the code for this checkpoint [here](https://github.com/mgtriffid/cotta-showcase/commit/29b70eb0c664c3456a3515f247a424501163fd20). 

But for the basic demonstration of Effects, this is enough.

The remaining part of this document is just a straightforward exercise to make
bullets move and to just get hands dirty in code once agan, feel free to skip,
you can just checkout the [next revision](https://github.com/mgtriffid/cotta-showcase/commit/099cc58fdcd7b558f956955863682aafa6bcc8e4)
of `cotta-showcase`.

### Moving bullets

So what we have to do is add a VelocityComponent and move bullets.

```kotlin
interface VelocityComponent : MutableComponent<VelocityComponent> {
    var x: Float
    var y: Float
}
```

Since we already have MovementSystem, it makes sense to extract the movement
control logic to a separate system. We will call it `MovementControlSystem`. And
what the original `MovementSystem` will do is to move entities using their 
`VelocityComponent`.

```kotlin
@Predicted
class MovementControlSystem : EntityProcessingSystem {
    override fun process(e: Entity, ctx: EntityProcessingContext) {
        if (e.hasComponent(ControllableComponent::class) &&
            e.hasComponent(VelocityComponent::class) &&
            e.hasComponent(PositionComponent::class)
        ) {
            val controllable = e.getComponent(ControllableComponent::class)
            val velocity = e.getComponent(VelocityComponent::class)
            velocity.x = when (controllable.direction) {
                WalkingDirections.LEFT -> -300f
                WalkingDirections.RIGHT -> 300f
                else -> 0f
            }
            velocity.y = when (controllable.direction) {
                WalkingDirections.UP -> 300f
                WalkingDirections.DOWN -> -300f
                else -> 0f
            }
            val position = e.getComponent(PositionComponent::class)
            position.orientation = when (controllable.direction) {
                WalkingDirections.UP -> Orientation.UP
                WalkingDirections.RIGHT -> Orientation.RIGHT
                WalkingDirections.DOWN -> Orientation.DOWN
                WalkingDirections.LEFT -> Orientation.LEFT
                else -> position.orientation
            }
        }
    }
}
```

```kotlin
@Predicted
class MovementSystem : EntityProcessingSystem {
    override fun process(e: Entity, ctx: EntityProcessingContext) {
        if (e.hasComponent(VelocityComponent::class) &&
            e.hasComponent(PositionComponent::class)
        ) {
            val velocity = e.getComponent(VelocityComponent::class)
            val position = e.getComponent(PositionComponent::class)
            position.x += velocity.x * ctx.clock().delta()
            position.y += velocity.y * ctx.clock().delta()
        }
    }
}
```

And since we're now using `VelocityComponent`, we need to add it to both players
and bullets:

```kotlin
class ShowcasePlayersHandler : PlayersHandler {
    override fun onEnterGame(playerId: PlayerId, entities: Entities) {
        entities.create(ownedBy = Entity.OwnedBy.Player(playerId)).apply {
            // ...
            addComponent(createVelocityComponent(
                x = 0f,
                y = 0f
            ))
        }
    }

    // ...
}
```

```kotlin
class ShootEffectConsumer : EffectsConsumerSystem<ShootEffect> {
    override val effectType: Class<ShootEffect> = ShootEffect::class.java

    override fun handle(e: ShootEffect, ctx: EffectProcessingContext) {
        val shooter = ctx.entities().get(e.shooterId) ?: return
        val position = shooter.getComponent(PositionComponent::class)
        val bullet = ctx.createEntity()
        bullet.addComponent(createPositionComponent(position.x, position.y, position.orientation))
        val velX = when (position.orientation) {
            Orientation.RIGHT -> 800f
            Orientation.LEFT -> -800f
            else -> 0f
        }
        val velY = when (position.orientation) {
            Orientation.UP -> 800f
            Orientation.DOWN -> -800f
            else -> 0f
        }
        bullet.addComponent(createVelocityComponent(velX, velY))
    }
}
```

This is enough. Again, `gradle build`, `gradle server:run`, `gradle lwjgl3:run`
and see bullets moving.
