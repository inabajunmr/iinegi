module Main exposing (main)

import Browser
import File
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
    , files : List File.File
    }


type NegiState
    = Init
    | Waiting
    | Loaded (List Negi)
    | Failed Http.Error


init : () -> ( Model, Cmd Msg )
init _ =
    ( Model "" Init []
    , Http.get
        { url = "http://0.0.0.0:8080/negi?after=1605367129000" -- 15 Nov 2020 17:19:15
        , expect = Http.expectJson Received negisDecoder
        }
    )



-- UPDATE


type Msg
    = Received (Result Http.Error (List Negi))
    | SelectImage (List File.File)
    | Upload
    | Uploaded (Result Http.Error ())


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        Received (Ok negi) ->
            ( { model | negiState = Loaded negi }, Cmd.none )

        Received (Err e) ->
            ( { model | negiState = Failed e }, Cmd.none )

        -- TODO
        Uploaded (Ok ok) ->
            ( model, Cmd.none )

        Uploaded (Err e) ->
            ( model, Cmd.none )

        SelectImage files ->
            ( { model | files = files }, Cmd.none )

        Upload ->
            ( model, upload model.files )



-- TODO
-- TODO https://package.elm-lang.org/packages/elm/http/latest/Http#request


upload : List File.File -> Cmd Msg
upload files =
    case List.head files of
        Just file ->
            Http.request
                { method = "POST"
                , headers = []
                , url = "http://0.0.0.0:8080/negi"
                , body =
                    Http.multipartBody
                        [ Http.stringPart "description" "Test", Http.filePart "image" file ]
                , expect = Http.expectWhatever Uploaded
                , timeout = Nothing
                , tracker = Nothing
                }

        Nothing ->
            Cmd.none



-- VIEW
-- https://elm-lang.org/examples/upload


view : Model -> Html Msg
view model =
    div []
        [ form model
        , case model.negiState of
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
        , text (Debug.toString model.files)
        ]


form : Model -> Html Msg
form model =
    div []
        [ input
            [ type_ "file"
            , multiple True
            , on "change" (D.map SelectImage filesDecoder)
            ]
            []
        , button [ onClick Upload ] [ text "Submit" ]
        ]


filesDecoder : D.Decoder (List File.File)
filesDecoder =
    D.at [ "target", "files" ] (D.list File.decoder)



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
