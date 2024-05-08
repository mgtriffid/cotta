# About
Cotta is a framework for building **fast-paced multiplayer games** in Java. It
makes netcode easy and allows developers to deliver smooth gameplay experience
almost regardless of network conditions.

It is well known that netcode is a hard part of game development, but there are
not too many libraries for Java ecosystem. Cotta fills this gap.

# For whom?
Cotta fits needs of Java game developers who build fast room-based multiplayer
games:
- Shooters, be it top-down or first-person: those are games where instant input
feedback on client is crucial, as well as lag compensation for hitscan weapons
- MOBAs, which are not very different from top-down shooters if we talk about
responsiveness and smoothness
- Probably, fightings
- Twitchy multiplayer platformers

[//]: # (Cotta **doesn't really suit** needs of those who work on)

[//]: # (- turn-based games: just use whatever protocol, you don't need reliable UDP)

[//]: # (- real-time strategies: most would run fine without client-side prediction and )

[//]: # (lag compensation, and the ECS engine of Cotta is not a state-of-art thing &#40;yet&#41;, )

[//]: # (so use something else)

[//]: # (- MMOs where the world is huge: Cotta simulates the whole world identically on)

[//]: # (Server and all Clients, and it doesn't make sense for MMOs.)

# Documentation
- [Quick start](quick-start.md)
- [Effects](effects.md)
- [Architecture overview](architecture-overview.md)
- [User guide](user-guide.md)

# Inspired by
- A GDC... No, **THE** GDC [talk by Blizzard about Overwatch netcode](https://www.youtube.com/watch?v=W3aieHjyNvw)
- [Photon Quantum](https://www.photonengine.com/quantum#)
- Glenn Fielder's [articles](https://gafferongames.com/)

# Features
- Built-in Entity-Component-System engine
- Exact same simulation on Client and Server
- Reliable UDP
- Client-side prediction
- Lag compensation
- Linear interpolation for clients
- Server-side reconciliation
- Adapting to changing network conditions like latency spikes or packet loss
