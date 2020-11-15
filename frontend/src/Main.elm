module Main exposing (main)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import Json.Decode as D exposing (..)


main : Program () Model Msg
main =
    Browser.element
        { init = init
        , view = view
        , update = update
        , subscriptions = \_ -> Sub.none
        }



-- MODEL


type alias Model =
    { input : String
    , negiState : NegiState
    }


type NegiState
    = Init
    | Waiting
    | Loaded (List Negi)
    | Failed Http.Error


init : () -> ( Model, Cmd Msg )
init _ =
    ( Model "" Init
    , Http.get
        { url = "http://0.0.0.0:8080/negi?after=1605367129000" -- 15 Nov 2020 17:19:15
        , expect = Http.expectJson Receive negisDecoder
        }
    )



-- UPDATE


type Msg
    = Receive (Result Http.Error (List Negi))


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        Receive (Ok user) ->
            ( { model | negiState = Loaded user }, Cmd.none )

        Receive (Err e) ->
            ( { model | negiState = Failed e }, Cmd.none )



-- VIEW


view : Model -> Html Msg
view model =
    div []
        [ case model.negiState of
            Init ->
                text ""

            Waiting ->
                text "Waiting..."

            Loaded negis ->
                ul []
                    (List.map
                        (\n -> li [] [ text n.description ])
                        negis
                    )

            Failed e ->
                div [] [ text (Debug.toString e) ]
        ]



-- DATA


type alias Negi =
    { description : String
    }


negisDecoder : Decoder (List Negi)
negisDecoder =
    D.list negiDecoder


negiDecoder : Decoder Negi
negiDecoder =
    D.map Negi
        (D.field "description" D.string)
