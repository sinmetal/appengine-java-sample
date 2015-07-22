# taskqueue-sample

## Overview

Compute Engineが、Service Accountを利用してpull queueにinsertしたtaskを、App Engineでleaseするサンプル。

## Caution

Compute EngineのService Accountを利用するので、localでは動かない
Compute Engineインスタンス生成時に、TaskQueueのScopeをEnableにしないと動かない