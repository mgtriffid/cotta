# Cotta: Kotlin game development library that solves netcode.

## Overview
Cotta is an [ECS](https://en.wikipedia.org/wiki/Entity_component_system)-based
game development library for Java/Kotlin built with multiplayer in mind.
With Cotta, multiplayer just works. It implements client-side prediction, lag
compensation, rewind-and-replay, client-side interpolation, missing packets
handling.

Cotta only solves simulation and netcode, but it has nothing to do with graphics
or audio or physics.  It is supposed to be used with LibGDX, for LibGDX is
**the** game development framework of Java ecosystem. It also kind of assumes
usage of Kotlin, but you may also write your logic in Java, it is just that
Components are defined as Kotlin interfaces.

Inspired by:
- A GDC... No, **THE** GDC [talk by Blizzard about Overwatch netcode](https://www.youtube.com/watch?v=W3aieHjyNvw)
- [Photon Quantum](https://www.photonengine.com/quantum#)
- Glenn Fielder's [articles](https://gafferongames.com/)

## Features

### ECS-based
Cotta implements the Entity-Component-System pattern, thus allowing developers
to handle different parts of game logic in a clean and organized way.

### Client-side prediction
Cotta runs two simulations on Client: one authoritative, exact same as the one
Server runs, and one prediction simulation which runs ahead of the server and
processes local inputs before they were acknowledged by Server, guaranteeing an
instant response from controls and pleasant gameplay experience.

### Lag compensation
Cotta implements lag compensation by rewinding the simulation back in time to
the moment a Client saw. It means if you are building a shooter game, you can
"favor the attacker" and make it so that if a player pulls off a headshot, it
is registered on Server.

### Interpolation
Just drop one annotation on your component and Cotta will interpolate it for you
on the client side, so that graphics are not bogged down by the lower-than-FPS
simulation rate.

### Deterministic lockstep
Cotta works in ticks, which allows the simulation to be exact same on the Server
and all Clients.

## Documentation
See the documentation at https://mgtriffid.github.io/cotta/

## Current state of the project
Cotta is neither in the early stages of development nor actually
production-ready. Almost all APIs are subject to change, and it will remain so,
until Cotta hits 1.x.x version. However, the APIs are intentionally limited and
simple, so that whenever a new API drops, it is easy and straightforward to
migrate to it.

Netcode is powered by KryoNet. Reliable UDP is built on top of it, and there are
some tricks to handle missing packets and spikes.

The ECS engine is not a state-of-art thing yet, but it is simple and works.
Meaning it is far from optimal, but making it blazing fast is one of the top
priorities.

## Roadmap (in no particular order)
- Extensive source code documentation
- Extensive documentation, tutorials, examples (now there's just a quick-start
guide)
- Refining interfaces and APIs
- Optimization
  - More efficient ECS engine
  - Garbage reduction
  - Adding (optional) GZip compression to the data being sent over the wire
- Extensive testing: to conduct and document experiments showing what happens
when Cotta faces
  - large number of entities
  - large number of clients
  - large data volumes sent over the wire
  - high packet loss or latency spikes (tested locally with emulated issues, so
far so good, it handles some packet loss well, adapts to changing latency, etc)
- Adding more features
  - More interpolation options
  - Sub-tick interpolation (this one is of lower priority, as eSports titles are
statistically rarely written with LibGDX)
  - ECS families similar to Ashley's ones
